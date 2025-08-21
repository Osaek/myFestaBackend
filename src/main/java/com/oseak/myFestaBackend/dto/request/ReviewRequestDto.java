package com.oseak.myFestaBackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리뷰 생성,수정 요청")
public class ReviewRequestDto {

	@Schema(description = "회원 ID", example = "101")
	@NotNull(message = "memberId는 필수입니다.")
	private Long memberId;

	@Schema(description = "축제 ID", example = "2612919")
	@NotNull(message = "festaId는 필수입니다.")
	private Long festaId;

	@Schema(description = "평점 (0.0~5.0).", example = "4.5")
	@DecimalMin(value = "0.0", inclusive = true, message = "평점은 0.0 이상이어야 합니다.")
	@DecimalMax(value = "5.0", inclusive = true, message = "평점은 5.0 이하여야 합니다.")
	private Double score;

	@Schema(description = "리뷰 이미지 URL", example = "https://.../image.jpg")
	@Size(max = 500)
	private String imageUrl;

	@Schema(description = "리뷰 본문", example = "분위기 좋고 재밌었어요!")
	@Size(max = 1000)
	private String description;
}
