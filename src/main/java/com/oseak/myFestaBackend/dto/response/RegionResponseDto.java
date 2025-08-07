package com.oseak.myFestaBackend.dto.response;

import com.oseak.myFestaBackend.entity.Region;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionResponseDto {
	String regionName;
	Integer regionCode;

	public RegionResponseDto(Region region) {
		this.regionName = region.getRegionName();
		this.regionCode = region.getRegionCode();
	}
}
