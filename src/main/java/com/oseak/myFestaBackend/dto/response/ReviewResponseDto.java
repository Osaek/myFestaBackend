package com.oseak.myFestaBackend.dto;

import java.time.LocalDate;

import com.oseak.myFestaBackend.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponseDto {
	private Long festaId;
	private Long memberId;
	private Double score;
	private String imageUrl;
	private String description;
	private LocalDate createdAt;

	public static ReviewResponseDto from(Review review) {
		return ReviewResponseDto.builder()
			.festaId(review.getId().getFestaId())
			.memberId(review.getId().getMemberId())
			.score(review.getScore())
			.imageUrl(review.getImageUrl())
			.description(review.getDescription())
			.createdAt(review.getCreatedAt())
			.build();
	}
}
