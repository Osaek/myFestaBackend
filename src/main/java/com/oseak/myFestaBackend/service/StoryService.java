package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.util.ShortCodeUtil;
import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryVisibilityUpdateRequestDto;
import com.oseak.myFestaBackend.dto.response.StoryItem;
import com.oseak.myFestaBackend.entity.Story;
import com.oseak.myFestaBackend.repository.StoryRepository;
import com.oseak.myFestaBackend.repository.StorySpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

	private final StoryRepository storyRepository;

	@Transactional
	public Page<StoryItem> search(StorySearchRequestDto request, Long viewerMemberId) {
		Specification<Story> spec = StorySpecification.createSpecification(request, viewerMemberId);

		org.springframework.data.domain.Pageable pageable =
			org.springframework.data.domain.PageRequest.of(
				request.getValidPage(),
				request.getValidSize(),
				org.springframework.data.domain.Sort.by("createdAt").descending()
					.and(org.springframework.data.domain.Sort.by("storyId").descending())
			);

		Page<Story> page = storyRepository.findAll(spec, pageable);
		return page.map(StoryItem::from);
	}

	public StoryItem getStory(String storyCode, Long requesterMemberId) {
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
			return StoryItem.from(story);
		}

		// 스토리 비공개일 경우, 스토리 소유자와 요청자 일치여부 확인
		if (requesterMemberId == null) {
			throw new OsaekException(USER_UNAUTHORIZED_ACCESS);
		}

		if (!story.getMemberId().equals(requesterMemberId)) {
			throw new OsaekException(FORBIDDEN);
		}

		return StoryItem.from(story);
	}

	@Transactional
	public StoryItem updateStoryVisibility(StoryVisibilityUpdateRequestDto request, Long requesterId) {
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
			return StoryItem.from(story);
		}

		if (Boolean.TRUE.equals(target)) {
			story.openStory();
		} else {
			story.hideStory();
		}

		return StoryItem.from(story);
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

}
