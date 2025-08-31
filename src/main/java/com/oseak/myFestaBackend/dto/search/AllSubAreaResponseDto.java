package com.oseak.myFestaBackend.dto.search;

import com.oseak.myFestaBackend.entity.SubArea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "하위 지역 코드 항목(상위지역코드포함)")
public class AllSubAreaResponseDto {
	@Schema(description = "상위지역코드", example = "1")
	Integer areaCode;

	@Schema(description = "하위지역명", example = "강남")
	String subAreaName;

	@Schema(description = "하위지역코드", example = "1")
	Integer subAreaCode;

	public static AllSubAreaResponseDto from(SubArea subArea) {
		return AllSubAreaResponseDto.builder()
			.areaCode(subArea.getId().getAreaCode())
			.subAreaCode(subArea.getId().getSubAreaCode())
			.subAreaName(subArea.getSubAreaName())
			.build();

	}
}
