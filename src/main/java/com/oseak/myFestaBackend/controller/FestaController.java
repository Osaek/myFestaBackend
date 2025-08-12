package com.oseak.myFestaBackend.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ClientErrorCode;
import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.dto.FestaSimpleDto;
import com.oseak.myFestaBackend.dto.FestaSummaryDto;
import com.oseak.myFestaBackend.dto.request.FestaSearchRequest;
import com.oseak.myFestaBackend.dto.response.FestaDetailResponseDto;
import com.oseak.myFestaBackend.dto.response.FestaSearchItem;
import com.oseak.myFestaBackend.dto.response.FestaSearchResponse;
import com.oseak.myFestaBackend.entity.DevPickFesta;
import com.oseak.myFestaBackend.service.FestaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/festa")
public class FestaController {

	private final FestaService festaService;

	//TODO : 배치로 전환 완료. 테스트용 API
	@GetMapping("/fetch")
	public ResponseEntity<String> fetchAndSaveFestas(@RequestParam(required = false) Integer areaCode) {
		String eventStartDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		festaService.fetchAndSaveFestas(eventStartDate, areaCode);
		return ResponseEntity.ok("축제 데이터 수집 및 저장 완료");
	}

	@Operation(
		summary = "근처 축제 조회",
		description = "현재 위치(lat, lng)를 기준으로 특정 거리 내에 있는 축제를 조회합니다.",
		parameters = {
			@Parameter(name = "lat", description = "위도", required = true, example = "37.5665"),
			@Parameter(name = "lng", description = "경도", required = true, example = "126.9780"),
			@Parameter(name = "distance", description = "검색 반경 (km)", required = true, example = "10")
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "축제 목록 반환", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FestaSimpleDto.class)))
		}
	)
	@GetMapping("/nearby")
	public ResponseEntity<CommonResponse<List<FestaSimpleDto>>> getNearbyFestasIds(@RequestParam double lat,
		@RequestParam double lng,
		@RequestParam int distance) {
		return ResponseEntity.ok(CommonResponse.success(festaService.findNearbyFesta(lat, lng, distance)));
	}

	@Operation(
		summary = "축제 요약 정보 조회",
		description = "festaId 리스트를 받아 해당하는 축제들의 요약 정보를 반환합니다.",
		parameters = {
			@Parameter(name = "festaIds", description = "축제 festaId 리스트", required = true, example = "12345,67890")
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "요약 정보 리스트 반환", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FestaSummaryDto.class)))
		}
	)
	@GetMapping("/summary")
	public ResponseEntity<CommonResponse<List<FestaSummaryDto>>> getFestaSummaries(
		@RequestParam List<Long> festaIds) {
		return ResponseEntity.ok(CommonResponse.success(festaService.getFestaSummariesByFestaIds(festaIds)));
	}

	@Operation(
		summary = "랜덤 축제 조회",
		description = "요청한 개수만큼 랜덤으로 축제 데이터를 반환합니다.",
		parameters = {
			@Parameter(name = "count", description = "조회할 축제 수", required = true, example = "5")
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "랜덤 축제 리스트 반환", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FestaSimpleDto.class)))
		}
	)
	@GetMapping("/random")
	public ResponseEntity<CommonResponse<List<FestaSimpleDto>>> getRandomFestas(@RequestParam int count) {
		return ResponseEntity.ok(CommonResponse.success(festaService.getRandomFestas(count)));
	}

	@GetMapping()
	@Operation(summary = "축제 검색", description = "키워드로 축제를 검색합니다")
	@ApiResponse(
		responseCode = "200",
		description = "검색 성공",
		content = @Content(schema = @Schema(implementation = CommonResponse.class))
	)
	public ResponseEntity<CommonResponse<FestaSearchResponse>> searchFestas(
		@Parameter(description = "축제 검색 조건") @ModelAttribute FestaSearchRequest request) {
		log.debug("받은 검색 요청: areaCode={}, subAreaCode={}, keyword={}",
			request.getAreaCode(), request.getSubAreaCode(), request.getKeyword());

		log.debug("전체 요청 객체: {}", request);
		Page<FestaSearchItem> festas = festaService.search(request);
		FestaSearchResponse festaSearchResponse = FestaSearchResponse.from(festas);

		return ResponseEntity.ok(CommonResponse.success(festaSearchResponse));
	}

	@GetMapping("/{festaId}/detail")
	@Operation(summary = "축제 상세 조회", description = "단건의 축제 상세 정보를 조회합니다.")
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공",
		content = @Content(schema = @Schema(implementation = CommonResponse.class))
	)
	public ResponseEntity<CommonResponse<FestaDetailResponseDto>> getFestaDetail(
		@Parameter(
			description = "조회할 축제의 ID",
			required = true,
			example = "3481597"
		)
		@PathVariable Long festaId) {
		log.debug("상세 조회 요청: id={}", festaId);
		validateFesta(festaId);

		FestaDetailResponseDto festaDetail = festaService.getDetail(festaId);

		return ResponseEntity.ok(CommonResponse.success(festaDetail));
	}

	/**
	 * 축제 ID 유효성 검증
	 *
	 * @param festaId 검증할 축제 ID
	 */
	private void validateFesta(Long festaId) {
		log.debug("축제 ID 유효성 검증 시작: id={}", festaId);

		if (festaId == null) {
			log.debug("축제 ID가 null입니다");
			throw new OsaekException(ClientErrorCode.FESTA_ID_NULL);
		}

		if (festaId <= 0) {
			log.debug("유효하지 않은 축제 ID입니다: id={}", festaId);
			throw new OsaekException(ClientErrorCode.FESTA_ID_INVALID);
		}

		log.debug("축제 ID 유효성 검증 완료: id={}", festaId);
	}

	@Operation(
		summary = "개발자 추천 축제",
		description = "메인에 노출할 추천 축제를 반환합니다.",
		parameters = {
			@Parameter(name = "count", description = "가져올 개수 (기본 2)", required = false, example = "2")
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "추천 축제 리스트 반환",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = DevPickFesta.class))
			)
		}
	)
	@GetMapping("/picks")
	public ResponseEntity<CommonResponse<List<DevPickFesta>>> getDeveloperPicks(
		@RequestParam(defaultValue = "2") int count) {
		return ResponseEntity.ok(CommonResponse.success(festaService.getDeveloperPicks(count)));
	}

}