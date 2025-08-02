package com.oseak.myFestaBackend.entity.dto;

import com.oseak.myFestaBackend.entity.Festa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestaSimpleDto {
	private Long contentId;
	private String festaName;
	private String imageUrl;

	public static FestaSimpleDto from(Festa festa) {
		return FestaSimpleDto.builder()
			.contentId(festa.getContentId())
			.festaName(festa.getFestaName())
			.imageUrl(festa.getImageUrl())
			.build();
	}
}