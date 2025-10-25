package com.oseak.myFestaBackend.dto.response;

import java.time.LocalDateTime;

import com.oseak.myFestaBackend.entity.Review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "리뷰 응답")
public class ReviewResponseDto {
	@Schema(description = "축제 ID", example = "2612919")
	private Long festaId;

	@Schema(description = "축제 명", example = "이건 테스트 축제")
	private String festaName;

	@Schema(description = "회원 ID", example = "101")
	private Long memberId;

	@Schema(description = "회원 닉네임", example = "파란 금붕어")
	private String memberNickname;

	@Schema(description = "회원 프로필 이미지 URL", example = "https://example.com/profile.jpg")
	private String memberProfileUrl;

	@Schema(description = "평점 (0.0~5.0)", example = "4.5")
	private Double score;

	@Schema(description = "리뷰 이미지 URL", example = "https://example.com/review.jpg")
	private String imageUrl;

	@Schema(description = "리뷰 본문", example = "전곡성도 좋고 즐길거리도 많아서 아이들하고 가기도 좋았습니다.")
	private String description;

	@Schema(description = "작성일", example = "2025-01-20")
	private LocalDateTime createdAt;

	public static ReviewResponseDto of(Review review, String memberNickname, String memberProfileUrl) {
		return ReviewResponseDto.builder()
			.festaId(review.getId().getFestaId())
			.festaName(review.getFesta().getFestaName())
			.memberId(review.getId().getMemberId())
			.memberNickname(memberNickname)
			.memberProfileUrl(memberProfileUrl)
			.score(review.getScore())
			.imageUrl(review.getImageUrl())
			.description(review.getDescription())
			.createdAt(review.getCreatedAt())
			.build();
	}
}
