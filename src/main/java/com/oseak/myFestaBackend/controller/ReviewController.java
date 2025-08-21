package com.oseak.myFestaBackend.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.dto.ReviewResponseDto;
import com.oseak.myFestaBackend.dto.request.ReviewRequestDto;
import com.oseak.myFestaBackend.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Validated
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(
		summary = "리뷰 생성",
		description = "회원이 특정 축제에 대해 리뷰(점수/이미지/본문)를 작성",
		responses = {
			@ApiResponse(responseCode = "200", description = "리뷰 생성 성공",
				content = @Content(schema = @Schema(implementation = CommonResponse.class)))
		}
	)
	@PostMapping
	public ResponseEntity<CommonResponse<Void>> create(@Valid @RequestBody ReviewRequestDto req) {
		if (req.getScore() == null) {
			throw new OsaekException(ServerErrorCode.MISSING_REQUIRED_FIELD);
		}
		reviewService.createReview(req.getMemberId(), req.getFestaId(), req.getScore(), req.getImageUrl(),
			req.getDescription());
		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@Operation(
		summary = "리뷰 수정",
		description = "리뷰 수정",
		responses = {
			@ApiResponse(responseCode = "200", description = "리뷰 수정 성공",
				content = @Content(schema = @Schema(implementation = CommonResponse.class)))
		}
	)
	@PutMapping
	public ResponseEntity<CommonResponse<Void>> update(@Valid @RequestBody ReviewRequestDto req) {
		reviewService.updateReview(req.getMemberId(), req.getFestaId(), req.getScore(), req.getImageUrl(),
			req.getDescription());
		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@Operation(
		summary = "리뷰 삭제",
		description = "리뷰 삭제",
		parameters = {
			@Parameter(name = "memberId", description = "회원 ID", required = true, example = "101"),
			@Parameter(name = "festaId", description = "축제 ID", required = true, example = "2612919")
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "리뷰 삭제 성공",
				content = @Content(schema = @Schema(implementation = CommonResponse.class)))
		}
	)
	@DeleteMapping
	public ResponseEntity<CommonResponse<Void>> delete(@RequestParam Long memberId, @RequestParam Long festaId) {
		reviewService.deleteReview(memberId, festaId);
		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@Operation(
		summary = "축제별 리뷰 목록 조회",
		description = "특정 축제에 대한 리뷰를 페이징으로 조회합니다.",
		parameters = {
			@Parameter(name = "festaId", description = "축제 ID", required = true, example = "2612919"),
			@Parameter(name = "page", description = "페이지 번호(0부터 시작)", example = "0"),
			@Parameter(name = "size", description = "페이지 크기(얼마나 조회할지)", example = "10"),
			@Parameter(name = "sort", description = "정렬 기준(latest,oldest,highest,lowest) latest가 default", example = "latest")
		}
	)
	@GetMapping("/detail")
	public ResponseEntity<CommonResponse<Page<ReviewResponseDto>>> getByFesta(
		@RequestParam Long festaId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "latest") String sort
	) {
		Page<ReviewResponseDto> result = reviewService.getReviewsByFesta(festaId, page, size, sort);
		return ResponseEntity.ok(CommonResponse.success(result));
	}
}
