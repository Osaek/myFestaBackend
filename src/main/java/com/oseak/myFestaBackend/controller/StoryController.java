package com.oseak.myFestaBackend.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.common.util.SecurityUtil;
import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryUploadRequestDto;
import com.oseak.myFestaBackend.dto.request.StoryVisibilityUpdateRequestDto;
import com.oseak.myFestaBackend.dto.response.StoryItem;
import com.oseak.myFestaBackend.dto.response.StorySearchResponseDto;
import com.oseak.myFestaBackend.service.StoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

	private final StoryService storyService;

	@PostMapping("/upload")
	public ResponseEntity<CommonResponse<Void>> uploadStory(@RequestParam("files") List<MultipartFile> file) {

		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "스토리 업로드", description = "이미지/비디오 파일과 함께 축제 정보를 업로드합니다.")
	public ResponseEntity<CommonResponse<String>> uploadStory(@ModelAttribute StoryUploadRequestDto request) {
		// TODO: 비즈니스 로직 완성 후 연결
		// String storyCode = storyService.uploadStory(request);
		String storyCode = "1234567890";
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storyCode));
	}

	@GetMapping
	@Operation(summary = "스토리 목록 조회", description = "여러 조건(축제명, 사용자)으로 스토리 목록을 조회합니다. 자신이 비공개처리한 스토리는 해당 회원이 검새해도 보이지 않도록 처리하여 비공개처리여부를 확인할 수 있습니다.")
	@ApiResponse(
		responseCode = "200",
		description = "검색 성공",
		content = @Content(schema = @Schema(implementation = CommonResponse.class))
	)
	public ResponseEntity<CommonResponse<StorySearchResponseDto>> searchStories(
		@ModelAttribute StorySearchRequestDto request) {
		log.debug("받은 요청: keyword={} memberId={}", request.getKeyword(), request.getMemberId());

		Long viewerMemberId = SecurityUtil.getCurrentUserId();
		log.debug("요청회원: {}", viewerMemberId);
		Page<StoryItem> stories = storyService.search(request, viewerMemberId);
		return ResponseEntity.ok(CommonResponse.success(StorySearchResponseDto.from(stories)));
	}

	@GetMapping("/{storyCode}")
	@Operation(summary = "스토리 단건 조회", description = "스토리 코드로 스토리를 조회합니다.")
	public ResponseEntity<CommonResponse<StoryItem>> getStory(
		@PathVariable @Parameter(description = "스토리 코드") String storyCode) {
		log.debug("스토리 단건 조회: storyCode:{}", storyCode);

		Long requesterMemberId = SecurityUtil.getCurrentUserId();
		StoryItem story = storyService.getStory(storyCode, requesterMemberId);
		return ResponseEntity.ok(CommonResponse.success(story));
	}

	@PostMapping("/visibility")
	@Operation(summary = "스토리 공개/비공개 설정", description = "스토리의 공개 여부를 변경합니다.")
	public ResponseEntity<CommonResponse<StoryItem>> updateStoryVisibility(
		@Valid @RequestBody StoryVisibilityUpdateRequestDto request) {
		log.debug("공개/비공개 설정: storyCode:{}, isOpen:{}", request.getStoryCode(), request.getIsOpen());

		Long requestMemberId = SecurityUtil.getCurrentUserId();
		StoryItem story = storyService.updateStoryVisibility(request, requestMemberId);
		return ResponseEntity.ok(CommonResponse.success(story));
	}

	@DeleteMapping("/{storyCode}")
	@Operation(summary = "스토리 삭제", description = "스토리를 논리 삭제합니다.")
	public ResponseEntity<CommonResponse<Void>> deleteStory(
		@PathVariable @Parameter(description = "스토리 코드") String storyCode) {
		log.debug("스토리 삭제: storyCode:{}", storyCode);

		Long requesterMemberId = SecurityUtil.getCurrentUserId();
		storyService.softDeleteStory(storyCode, requesterMemberId);
		return ResponseEntity.ok(CommonResponse.noContent());
	}
}