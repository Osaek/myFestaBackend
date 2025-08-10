package com.oseak.myFestaBackend.dto;

import java.time.LocalDate;

import com.oseak.myFestaBackend.entity.Festa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestaSummaryDto {
	private Long contentId;
	private String festaName;
	private String imageUrl;
	private String festaAddress;
	private LocalDate festaStartAt;
	private LocalDate festaEndAt;
	private String overview;

	public static FestaSummaryDto from(Festa festa) {
		return FestaSummaryDto.builder()
			.contentId(festa.getContentId())
			.festaName(festa.getFestaName())
			.imageUrl(festa.getImageUrl())
			.festaAddress(festa.getFestaAddress())
			.festaStartAt(festa.getFestaStartAt())
			.festaEndAt(festa.getFestaEndAt())
			.overview(festa.getOverview())
			.build();
	}
}