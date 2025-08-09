package com.oseak.myFestaBackend.dto.response;

import com.oseak.myFestaBackend.entity.Area;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "상위 지역 코드 항목")
public class AreaResponseDto {
	@Schema(description = "지역명", example = "서울")
	String areaName;

	@Schema(description = "지역코드", example = "1")
	Integer areaCode;

	public static AreaResponseDto of(Area area) {
		return AreaResponseDto.builder()
			.areaName(area.getAreaName())
			.areaCode(area.getAreaCode())
			.build();
	}
}
