package com.oseak.myFestaBackend.dto;

import com.oseak.myFestaBackend.entity.Festa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestaSimpleDto {
	private Long festaId;
	private String festaName;
	private String imageUrl;

	public static FestaSimpleDto from(Festa festa) {
		return FestaSimpleDto.builder()
			.festaId(festa.getFestaId())
			.festaName(festa.getFestaName())
			.imageUrl(festa.getImageUrl())
			.build();
	}
}