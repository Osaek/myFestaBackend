package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.dto.response.AllSubAreaResponseDto;
import com.oseak.myFestaBackend.dto.response.AreaResponseDto;
import com.oseak.myFestaBackend.dto.response.SubAreaResponseDto;
import com.oseak.myFestaBackend.entity.Area;
import com.oseak.myFestaBackend.entity.SubArea;
import com.oseak.myFestaBackend.repository.AreaRepository;
import com.oseak.myFestaBackend.repository.SubAreaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaService {

	private final AreaRepository areaRepository;
	private final SubAreaRepository subAreaRepository;

	/**
	 * 상위지역 코드 리스트 조회
	 *
	 * @return 상위지역코드 응답 DTO 리스트
	 */
	public List<AreaResponseDto> getAllAreas() {
		log.debug("상위 지역 코드 조회");
		List<Area> areas = areaRepository.findAll();

		return areas
			.stream()
			.map(AreaResponseDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 모든 하위 지역 코드 리스트 조회
	 *
	 * @return 하위 지역 코드 응답 DTO 리스트
	 */
	public List<AllSubAreaResponseDto> getSubAreas() {
		log.debug("모든 하위 지역 코드 조회");
		List<SubArea> areas = subAreaRepository.findAll();

		return areas
			.stream()
			.map(AllSubAreaResponseDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 특정 상위지역에 속한 하위 지역 코드 리스트 조회
	 *
	 * @param areaCode 상위 지역 코드
	 * @return 하위 지역 코드 응답 DTO 리스트
	 */
	public List<SubAreaResponseDto> getSubAreasByAreaCode(Integer areaCode) {
		log.debug("상위코드:{} 지역에 대한 하위 코드 조회", areaCode);
		if (!areaRepository.existsById(areaCode)) {
			log.error("상위코드:{} 존재하지 않음", areaCode);
			throw new OsaekException(AREA_CODE_NOT_FOUND);
		}

		return subAreaRepository.findByIdAreaCode(areaCode)
			.stream()
			.map(SubAreaResponseDto::from)
			.collect(Collectors.toList());
	}
}
