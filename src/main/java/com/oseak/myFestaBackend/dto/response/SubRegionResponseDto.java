package com.oseak.myFestaBackend.dto.response;

import com.oseak.myFestaBackend.entity.SubRegion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubRegionResponseDto {
	String subRegionName;
	Integer subRegionCode;

	public SubRegionResponseDto(SubRegion subRegion) {
		this.subRegionName = subRegion.getSubRegionName();
		this.subRegionCode = subRegion.getId().getRegionCode();
	}
}
