package com.oseak.myFestaBackend.dto.response;

import com.oseak.myFestaBackend.entity.SubArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "하위 지역 코드 항목")
public class SubAreaResponseDto {
	@Schema(description = "지역명", example = "서울")
	String subAreaName;

	@Schema(description = "지역코드", example = "1")
	Integer subAreaCode;

	public static SubAreaResponseDto from(SubArea subArea) {
		return SubAreaResponseDto.builder()
			.subAreaName(subArea.getSubAreaName())
			.subAreaCode(subArea.getId().getSubAreaCode())
			.build();
	}
}
