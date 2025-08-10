package com.oseak.myFestaBackend.common.response;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "페이징 정보")
public class PageInfo {
	@Schema(description = "전체 데이터 개수", example = "100")
	private final Long totalCount;

	@Schema(description = "현재 페이지", example = "0")
	private final Integer page;

	@Schema(description = "요청한 페이지 크기", example = "20")
	private final Integer size;

	public static PageInfo of(Page<?> page) {
		return PageInfo.builder()
			.totalCount(page.getTotalElements())
			.page(page.getNumber()) // Page는 0-based
			.size(page.getSize())
			.build();
	}
}