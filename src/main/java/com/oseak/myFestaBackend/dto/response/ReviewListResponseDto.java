package com.oseak.myFestaBackend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.oseak.myFestaBackend.common.response.PageInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리뷰 목록 조회 응답")
public class ReviewListResponseDto {

	@Schema(description = "페이징 정보")
	private PageInfo pageInfo;

	@Schema(description = "리뷰 목록")
	private List<ReviewResponseDto> reviews;

	public static ReviewListResponseDto from(Page<ReviewResponseDto> page) {
		return ReviewListResponseDto.builder()
			.pageInfo(PageInfo.of(page))
			.reviews(page.getContent())
			.build();
	}
}
