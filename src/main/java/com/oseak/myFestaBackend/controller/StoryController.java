package com.oseak.myFestaBackend.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.common.util.SecurityUtil;
import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryUploadRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryVisibilityUpdateRequestDto;
import com.oseak.myFestaBackend.dto.response.StoryItem;
import com.oseak.myFestaBackend.dto.response.StorySearchResponseDto;
import com.oseak.myFestaBackend.facade.StoryFacade;
import com.oseak.myFestaBackend.service.S3Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/stories")
@Tag(name = "Story API", description = "스토리 관련 API(Controller)")
public class StoryController {

	private final StoryFacade storyFacade;
	private final S3Service s3Service;

	// @PostMapping("/upload")
	// public ResponseEntity<CommonResponse<Void>> uploadStory(@RequestParam("file") MultipartFile file) {
	// 	log.debug("Story upload request received - file size: {}, content type: {}",
	// 		file.getSize(), file.getContentType());
	//
	// 	Long memberId = SecurityUtil.getCurrentUserId();
	//
	// 	storyService.uploadStory(file, memberId);
	// 	return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null));
	// }

	@PostMapping("/upload")
	@Operation(summary = "스토리 업로드", description = "이미지/비디오 파일과 함께 축제 정보를 업로드합니다.")
	public ResponseEntity<CommonResponse<StoryItem>> uploadStory(@ModelAttribute StoryUploadRequestDto request) {
		// TODO: 비즈니스 로직 완성 후 연결
		log.info("=== 받은 데이터 확인 ===");
		log.info("File: {}", request.getFile() != null ? request.getFile().getOriginalFilename() : "null");
		log.info("MemberId: {}", request.getMemberId());
		log.info("FestaId: {}", request.getFestaId());  // null인지 확인
		log.info("FestaName: {}", request.getFestaName());
		log.info("IsOpen: {}", request.getIsOpen());

		Long requesterMemberId = SecurityUtil.getCurrentUserId();
		StoryItem storyItem = storyFacade.uploadStoryAsync(request, requesterMemberId);
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storyItem));
	}

	@GetMapping
	@Operation(
		summary = "스토리 목록 조회",
		description = """
			여러 조건(축제, 사용자, 키워드)으로 스토리를 페이지네이션하여 조회합니다.
			
			• 기본(검색/타인 프로필): 공개 스토리만 노출
			• 내 프로필: includePrivateMine=true일 때만 내 비공개 포함
			• 삭제된 스토리(isDeleted=true)는 항상 제외
			정렬: createdAt DESC, storyId DESC
			
			요청 예시
			- 내 프로필(특정 축제, 비공개 포함)
			  GET /stories?memberId=123&includePrivateMine=true&festaId=987&page=0&size=20
			
			- 남의 프로필(특정 축제)
			  GET /stories?memberId=456&festaId=987&page=0&size=20
			
			- 검색 페이지(특정 축제 + 키워드)
			  GET /stories?festaId=987&keyword=불꽃놀이&page=0&size=20
			""",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "검색 성공",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = CommonResponse.class)
				)
			)
		}
	)
	@Parameters({
		@Parameter(name = "memberId", description = "조회 대상 회원 ID (내/타인 프로필 조회에 사용)", example = "123"),
		@Parameter(name = "includePrivateMine", description = "내 프로필에서 내 비공개 포함 여부(내 계정일 때만 유효)", example = "true"),
		@Parameter(name = "festaId", description = "특정 축제 ID 필터", example = "1390147"),
		@Parameter(name = "keyword", description = "축제명 키워드(대소문자 무시, LIKE 검색)", example = "불꽃놀이"),
		@Parameter(name = "page", description = "0부터 시작하는 페이지 번호", example = "0"),
		@Parameter(name = "size", description = "페이지 크기", example = "20")
	})
	public ResponseEntity<CommonResponse<StorySearchResponseDto>> searchStories(
		@ModelAttribute StorySearchRequestDto request
	) {
		log.debug("받은 요청: keyword={} memberId={} festaId={} includePrivateMine={}",
			request.getKeyword(), request.getMemberId(), request.getFestaId(), request.getIncludePrivateMine());

		Long viewerMemberId = SecurityUtil.getCurrentUserIdOrNull();
		log.debug("요청회원: {}", viewerMemberId);

		Page<StoryItem> stories = storyFacade.searchStories(request, viewerMemberId);
		return ResponseEntity.ok(CommonResponse.success(StorySearchResponseDto.from(stories)));
	}

	@GetMapping("/{storyCode}")
	@Operation(summary = "스토리 단건 조회", description = "스토리 코드로 스토리를 조회합니다.")
	public ResponseEntity<CommonResponse<StoryItem>> getStory(
		@PathVariable @Parameter(description = "스토리 코드") String storyCode) {
		log.debug("스토리 단건 조회: storyCode:{}", storyCode);

		Long requesterMemberId = SecurityUtil.getCurrentUserIdOrNull();
		StoryItem story = storyFacade.getStory(storyCode, requesterMemberId);
		return ResponseEntity.ok(CommonResponse.success(story));
	}

	@PostMapping("/visibility")
	@Operation(summary = "스토리 공개/비공개 설정", description = "스토리의 공개 여부를 변경합니다.")
	public ResponseEntity<CommonResponse<StoryItem>> updateStoryVisibility(
		@Valid @RequestBody StoryVisibilityUpdateRequestDto request) {
		log.debug("공개/비공개 설정: storyCode:{}, isOpen:{}", request.getStoryCode(), request.getIsOpen());

		Long requestMemberId = SecurityUtil.getCurrentUserId();
		StoryItem story = storyFacade.updateStoryVisibility(request, requestMemberId);
		return ResponseEntity.ok(CommonResponse.success(story));
	}

	@DeleteMapping("/{storyCode}")
	@Operation(summary = "스토리 삭제", description = "스토리를 논리 삭제합니다.")
	public ResponseEntity<CommonResponse<Void>> deleteStory(
		@PathVariable @Parameter(description = "스토리 코드") String storyCode) {
		log.debug("스토리 삭제: storyCode:{}", storyCode);

		Long requesterMemberId = SecurityUtil.getCurrentUserId();
		storyFacade.softDeleteStory(storyCode, requesterMemberId);
		return ResponseEntity.ok(CommonResponse.noContent());
	}
}