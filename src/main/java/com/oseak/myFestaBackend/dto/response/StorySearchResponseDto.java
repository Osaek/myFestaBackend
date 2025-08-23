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
@Schema(description = "스토리 조회 응답")
public class StorySearchResponseDto {
	@Schema(description = "페이징정보")
	private PageInfo pageInfo;

	@Schema(description = "스토리목록", example = "[]")
	private List<StoryItem> stories;

	public static StorySearchResponseDto from(Page<StoryItem> stories) {
		return StorySearchResponseDto.builder()
			.stories(stories.getContent())
			.pageInfo(PageInfo.of(stories))
			.build();
	}
}
