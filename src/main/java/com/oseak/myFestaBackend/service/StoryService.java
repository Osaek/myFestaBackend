package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.util.ShortCodeUtil;
import com.oseak.myFestaBackend.dto.Thumbnail;
import com.oseak.myFestaBackend.dto.ThumbnailResult;
import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryUploadRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryVisibilityUpdateRequestDto;
import com.oseak.myFestaBackend.dto.response.StoryItem;
import com.oseak.myFestaBackend.entity.Story;
import com.oseak.myFestaBackend.entity.enums.MediaType;
import com.oseak.myFestaBackend.entity.enums.ProcessingStatus;
import com.oseak.myFestaBackend.event.MediaProcessingCompletedEvent;
import com.oseak.myFestaBackend.event.MediaProcessingEvent;
import com.oseak.myFestaBackend.generator.ThumbnailGenerator;
import com.oseak.myFestaBackend.repository.StoryRepository;
import com.oseak.myFestaBackend.repository.StorySpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

	private final StoryRepository storyRepository;
	private final ThumbnailGenerator thumbnailGenerator;
	private final S3Service s3Service;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Page<Story> searchStories(StorySearchRequestDto request, Long viewerMemberId) {
		Specification<Story> spec = StorySpecification.createSpecification(request, viewerMemberId);

		Pageable pageable =
			PageRequest.of(
				request.getValidPage(),
				request.getValidSize(),
				Sort.by("createdAt").descending()
					.and(Sort.by("storyId").descending())
			);

		return storyRepository.findAll(spec, pageable);
	}

	public Story getStoryEntity(String storyCode, Long requesterMemberId) {
		final long storyId;
		try {
			storyId = ShortCodeUtil.decode(storyCode);
		} catch (Exception e) {
			throw new OsaekException(SHORT_CODE_INVALID);
		}

		Story story = storyRepository.findById(storyId)
			.orElseThrow(() -> new OsaekException(STORY_NOT_FOUND));

		// 스토리 공개일 경우
		if (story.getIsOpen()) {
			return story;
		}

		// 스토리 비공개일 경우, 스토리 소유자와 요청자 일치여부 확인
		if (requesterMemberId == null) {
			throw new OsaekException(USER_UNAUTHORIZED_ACCESS);
		}

		if (!story.getMemberId().equals(requesterMemberId)) {
			throw new OsaekException(FORBIDDEN);
		}

		return story;
	}

	@Transactional
	public Story updateStoryVisibilityEntity(StoryVisibilityUpdateRequestDto request, Long requesterId) {
		if (requesterId == null)
			throw new OsaekException(USER_UNAUTHORIZED_ACCESS);
		log.debug("storyCode: {}", request.getStoryCode());
		final long storyId;
		try {
			storyId = ShortCodeUtil.decode(request.getStoryCode());
		} catch (Exception e) {
			throw new OsaekException(SHORT_CODE_INVALID);
		}
		log.debug("storyId: {}", storyId);
		Story story = storyRepository.findById(storyId)
			.orElseThrow(() -> new OsaekException(STORY_NOT_FOUND));

		// 삭제된 스토리는 변경 불가
		if (Boolean.TRUE.equals(story.getIsDeleted())) {
			throw new OsaekException(STORY_NOT_FOUND);
		}

		// 소유자 검증
		if (!story.getMemberId().equals(requesterId)) {
			throw new OsaekException(FORBIDDEN);
		}

		// 멱등 처리: 목표 상태와 다를 때만 변경
		Boolean target = request.getIsOpen();
		if (Objects.equals(target, story.getIsOpen())) {
			return story;
		}

		if (Boolean.TRUE.equals(target)) {
			story.openStory();
		} else {
			story.hideStory();
		}

		return story;
	}

	@Transactional
	public void softDeleteStory(String storyCode, Long requesterMemberId) {
		if (requesterMemberId == null) {
			throw new OsaekException(USER_UNAUTHORIZED_ACCESS);
		}

		final long storyId;
		try {
			storyId = ShortCodeUtil.decode(storyCode);
		} catch (Exception e) {
			throw new OsaekException(SHORT_CODE_INVALID);
		}

		Story story = storyRepository.findById(storyId)
			.orElseThrow(() -> new OsaekException(STORY_NOT_FOUND));

		// 이미 삭제된 경우: 멱등 처리
		if (Boolean.TRUE.equals(story.getIsDeleted())) {
			return;
		}

		// 소유자 검증
		if (!story.getMemberId().equals(requesterMemberId)) {
			throw new OsaekException(FORBIDDEN);
		}

		// 논리 삭제
		story.softDelete();
	}

	public void deleteStory() {
		List<Story> deletedStories = storyRepository.findAllByIsDeletedTrue();

		if (deletedStories.isEmpty()) {
			log.info("삭제할 스토리가 없습니다.");
			return;
		}

		int totalCount = deletedStories.size();
		List<Long> storyIds = deletedStories.stream()
			.map(Story::getStoryId)
			.toList();

		storyRepository.deleteAll(deletedStories);

		log.info("총 {}건 삭제 완료. 삭제 ID 목록={}", totalCount, storyIds);
	}

	public Story uploadStory(MultipartFile file, Long memberId) {
		String tempMediaId = UUID.randomUUID().toString();

		try {
			// 1. 미디어 타입 확인
			MediaType mediaType = MediaType.detectFromFile(file);
			log.info("Starting story upload for mediaId: {}, mediaType: {}", tempMediaId, mediaType);

			// 2. 썸네일 생성
			ThumbnailResult result = thumbnailGenerator.generateThumbnails(file, tempMediaId, mediaType);

			// 3. ⭐ 결과 상세 검증 (모든 null 체크)
			if (result == null) {
				log.error("ThumbnailResult is null for mediaId: {}", tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			log.info("ThumbnailResult - mediaType: {}, thumbnailsCount: {}",
				result.getMediaType(),
				result.getThumbnails() != null ? result.getThumbnails().size() : "null");

			if (result.getThumbnails() == null || result.getThumbnails().isEmpty()) {
				log.error("No thumbnails generated for mediaId: {}", tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			// 4. ⭐ 첫 번째 썸네일 안전하게 가져오기
			Thumbnail firstThumbnail = result.getThumbnails().get(0);
			if (firstThumbnail == null) {
				log.error("First thumbnail is null for mediaId: {}", tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			String thumbnailPath = firstThumbnail.getLocalPath();
			log.info("First thumbnail localPath: {}", thumbnailPath);

			if (thumbnailPath == null || thumbnailPath.trim().isEmpty()) {
				log.error("Thumbnail localPath is null or empty for mediaId: {}", tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			// 5. ⭐ 파일 존재 확인
			File thumbnailFile = new File(thumbnailPath);
			if (!thumbnailFile.exists()) {
				log.error("Thumbnail file does not exist: {} for mediaId: {}", thumbnailPath, tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			if (thumbnailFile.length() == 0) {
				log.error("Thumbnail file is empty: {} for mediaId: {}", thumbnailPath, tempMediaId);
				throw new OsaekException(THUMBNAIL_CANT_CREATE);
			}

			log.info("Thumbnail file verified: {} (size: {} bytes)", thumbnailPath, thumbnailFile.length());

			// 6. 원본 파일 처리 (안전하게)
			String originalS3Url = null;
			String originalLocalPath = result.getOriginalLocalPath();
			if (originalLocalPath != null && !originalLocalPath.trim().isEmpty()) {
				File originalFile = new File(originalLocalPath);
				if (originalFile.exists() && originalFile.length() > 0) {
					originalS3Url = s3Service.uploadFile(originalFile);
					new File(originalLocalPath).delete();
					log.info("Uploaded original file to S3: {}", originalS3Url);
				} else {
					log.warn("Original file not found or empty: {}", originalLocalPath);
				}
			} else {
				log.warn("Original local path is null or empty for mediaId: {}", tempMediaId);
			}

			// 7. 썸네일 S3 업로드
			String thumbnailS3Url = s3Service.uploadFile(thumbnailFile);
			new File(thumbnailPath).delete();
			log.info("Uploaded thumbnail to S3: {}", thumbnailS3Url);

			// 8. Story 저장
			Story story = Story.builder()
				.storyS3Url(originalS3Url)      // 원본 파일 URL (null 가능)
				.thumbnailUrl(thumbnailS3Url)   // 썸네일 URL
				.memberId(memberId)
				.storyType(String.valueOf(mediaType))
				.festaId(1L)
				.isOpen(true)
				.isDeleted(false)
				.build();

			Story savedStory = storyRepository.save(story);
			log.info("Successfully saved story with ID: {}", savedStory.getStoryId());

			return savedStory;

		} catch (OsaekException e) {
			log.error("Business error during story upload for mediaId: {}", tempMediaId, e);
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error during story upload for mediaId: {}", tempMediaId, e);
			throw new OsaekException(THUMBNAIL_CANT_CREATE);
		} finally {
			// 9. 임시 파일 정리
		}

		// // 1. 임시 UUID로 미디어 처리 먼저 수행
		// String tempMediaId = UUID.randomUUID().toString();
		// MediaType mediaType = MediaType.detectFromFile(file);
		//
		// ThumbnailResult result = thumbnailGenerator.generateThumbnails(file, tempMediaId, mediaType);
		// File thumbnail = new File(result.getThumbnails().get(0).getLocalPath());
		// File original = new File(result.getCompressedOriginalPath());
		//
		// // 2. S3 업로드
		// String originalS3Url = s3Service.uploadFile(original);
		// String thumbnailS3Url = s3Service.uploadFile(thumbnail);
		// // thumbnail = new File(result.getThumbnails().get(1).getLocalPath());
		// // s3Service.uploadFile(thumbnail);
		// // thumbnail = new File(result.getThumbnails().get(2).getLocalPath());
		// // s3Service.uploadFile(thumbnail);
		//
		// // 3. Story 저장 (모든 미디어 처리 완료 후)
		// Story story = Story.builder()
		// 	// .storyS3Url(originalS3Url)
		// 	.thumbnailUrl(thumbnailS3Url)
		// 	.memberId(memberId)
		// 	.storyType(String.valueOf(mediaType))
		// 	.festaId(1L)
		// 	.isOpen(true)
		// 	.isDeleted(false)
		// 	.build();
		//
		// return storyRepository.save(story);

	}

	public Story uploadStoryAsyncEntity(StoryUploadRequestDto requestDto, Long memberId) {
		// 1. 미디어 타입 확인
		MediaType mediaType = MediaType.detectFromFile(requestDto.getFile());
		log.debug("Starting story upload for mediaType: {}", mediaType);

		// 2. 스토리 저장
		Story story = Story.builder()
			.memberId(memberId)
			.storyType(String.valueOf(mediaType))
			.festaId(requestDto.getFestaId())
			.festaName(requestDto.getFestaName())
			.isOpen(requestDto.getIsOpen())
			.processingStatus(ProcessingStatus.PROCESSING.name())
			.build();

		Story savedStory = storyRepository.save(story);

		// 3. 비동기 처리 이벤트 발행
		eventPublisher.publishEvent(new MediaProcessingEvent(
			requestDto.getFile(),
			savedStory.getStoryId(),
			mediaType
		));

		// 4. 반환
		return savedStory;
	}

	@EventListener
	@Transactional
	public void handleMediaProcessingCompleted(MediaProcessingCompletedEvent event) {
		log.debug("Handling media processing completion for storyId: {}, status: {}",
			event.getStoryId(), event.getStatus());

		Story story = storyRepository.findById(event.getStoryId())
			.orElseThrow(() -> new OsaekException(STORY_NOT_FOUND));

		story.completeMediaProcessing(event.getOriginalS3Url(), event.getThumbnailS3Url(), event.getStatus().name());
		storyRepository.save(story);
		log.debug("Updated story {} with URLs and status: {}", event.getStoryId(), event.getStatus());
	}
}
