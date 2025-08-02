package com.oseak.myFestaBackend.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.entity.dto.FestaSimpleDto;
import com.oseak.myFestaBackend.entity.dto.FestaSummaryDto;
import com.oseak.myFestaBackend.service.FestaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/festival")
public class FestaController {

	private final FestaService festaService;

	//TODO : 배치로 전환 완료. 테스트용 API
	@GetMapping("/fetch")
	public ResponseEntity<String> fetchAndSaveFestivals(@RequestParam(required = false) Integer areaCode) {
		String eventStartDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		festaService.fetchAndSaveFestivals(eventStartDate, areaCode);
		return ResponseEntity.ok("축제 데이터 수집 및 저장 완료");
	}

	@GetMapping("/nearby")
	public ResponseEntity<List<FestaSimpleDto>> getNearbyFestivalIds(@RequestParam double lat, @RequestParam double lng,
		@RequestParam int distance) {
		return ResponseEntity.ok(festaService.findNearbyFesta(lat, lng, distance));
	}

	@GetMapping("/summary")
	public ResponseEntity<List<FestaSummaryDto>> getFestivalSummaries(@RequestParam List<Long> ids) {
		return ResponseEntity.ok(festaService.getFestaSummariesByContentIds(ids));
	}

	@GetMapping("/random")
	public ResponseEntity<List<FestaSimpleDto>> getRandomFestivals(@RequestParam int count) {
		return ResponseEntity.ok(festaService.getRandomFestivals(count));
	}

}