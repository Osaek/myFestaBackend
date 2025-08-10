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
@Schema(description = "축제 검색 응답")
public class FestivalSearchResponse {
	@Schema(description = "페이징 정보")
	private PageInfo pageInfo;

	@Schema(description = "축제 목록", example = "[]")
	private List<FestivalSearchItem> festivals;

	public static FestivalSearchResponse from(Page<FestivalSearchItem> page) {
		return FestivalSearchResponse.builder()
			.festivals(page.getContent())
			.pageInfo(PageInfo.of(page))
			.build();
	}
}
