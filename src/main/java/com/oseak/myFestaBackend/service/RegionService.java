package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.dto.response.RegionResponseDto;
import com.oseak.myFestaBackend.dto.response.SubRegionResponseDto;
import com.oseak.myFestaBackend.entity.Region;
import com.oseak.myFestaBackend.entity.SubRegion;
import com.oseak.myFestaBackend.repository.RegionRepository;
import com.oseak.myFestaBackend.repository.SubRegionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

	private final RegionRepository regionRepository;
	private final SubRegionRepository subRegionRepository;

	/**
	 * 상위지역 코드 리스트 조회
	 * @return 상위지역코드 응답 DTO 리스트
	 */
	public List<RegionResponseDto> getAllRegions() {
		List<Region> regions = regionRepository.findAll();

		return regions
			.stream()
			.map(RegionResponseDto::new)
			.collect(Collectors.toList());
	}

	/**
	 * 모든 하위 지역 코드 리스트 조회
	 * @return 하위 지역 코드 응답 DTO 리스트
	 */
	public List<SubRegionResponseDto> getSubRegions() {
		List<SubRegion> regions = subRegionRepository.findAll();

		return regions
			.stream()
			.map(SubRegionResponseDto::new)
			.collect(Collectors.toList());
	}

	/**
	 * 특정 상위지역에 속한 하위 지역 코드 리스트 조회
	 * @param regionCode 상위 지역 코드
	 * @return 하위 지역 코드 응답 DTO 리스트
	 */
	public List<SubRegionResponseDto> getSubRegionsByRegionCode(Integer regionCode) {
		log.debug("상위코드:{} 지역에 대한 하위 코드 조회", regionCode);
		if (!regionRepository.existsById(regionCode)) {
			log.error("상위코드:{} 존재하지 않음", regionCode);
			throw new OsaekException(REGION_CODE_NOT_FOUND);
		}

		return subRegionRepository.findByIdRegionCode(regionCode)
			.stream()
			.map(SubRegionResponseDto::new)
			.collect(Collectors.toList());
	}
}
