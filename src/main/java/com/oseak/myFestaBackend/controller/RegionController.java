package com.oseak.myFestaBackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.dto.response.RegionResponseDto;
import com.oseak.myFestaBackend.dto.response.SubRegionResponseDto;
import com.oseak.myFestaBackend.service.RegionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Region API", description = "지역코드 관련 API(Controller)")
@Slf4j
@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {

	private final RegionService regionService;

	@Operation(
		summary = "상위 지역 코드 요청 API",
		description = "축제 정보 조회를 위한 상위 지역 코드 요청 API입니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "상위 지역 코드 조회 성공"),
			@ApiResponse(responseCode = "500", description = "상위 지역 코드 조회 실패")
		}
	)
	@GetMapping
	public ResponseEntity<CommonResponse<List<RegionResponseDto>>> getRegions() {
		List<RegionResponseDto> regions = regionService.getAllRegions();
		return ResponseEntity.ok(CommonResponse.success(regions));
	}

	@Operation(
		summary = "모든 하위 지역 코드 요청 API",
		description = "축제 정보 조회를 위한 하위 지역 코드 요청 API입니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "하위 지역 코드 조회 성공"),
			@ApiResponse(responseCode = "500", description = "하위 지역 코드 조회 실패")
		}
	)
	@GetMapping("/sub-regions")
	public ResponseEntity<CommonResponse<List<SubRegionResponseDto>>> getSubRegions() {
		List<SubRegionResponseDto> subRegions = regionService.getSubRegions();
		return ResponseEntity.ok(CommonResponse.success(subRegions));
	}

	@Operation(
		summary = "특정 상위지역에 속하는 하위 지역 코드 요청 API",
		description = "축제 정보 조회를 위한 하위 지역 코드 요청 API입니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "하위 지역 코드 조회 성공"),
			@ApiResponse(responseCode = "500", description = "하위 지역 코드 조회 실패")
		}
	)
	@GetMapping("/{regionCode}/sub-regions")
	public ResponseEntity<CommonResponse<List<SubRegionResponseDto>>> getSubRegionsByRegionCode(
		@Parameter(description = "상위 지역을 식별하는 고유 코드", required = true, example = "1")
		@PathVariable("regionCode") Integer regionCode) {
		List<SubRegionResponseDto> subRegions = regionService.getSubRegionsByRegionCode(regionCode);
		return ResponseEntity.ok(CommonResponse.success(subRegions));
	}
}