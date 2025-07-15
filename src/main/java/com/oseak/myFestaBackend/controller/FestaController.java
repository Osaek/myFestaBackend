package com.oseak.myFestaBackend.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.service.FestaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/festival")
public class FestaController {

	//TODO : 추후 컨트롤러 삭제 후 배치파일로 메소드 작동할 예정
	private final FestaService festaService;

	@GetMapping("/fetch")
	public ResponseEntity<String> fetchAndSaveFestivals(@RequestParam(required = false) Integer areaCode) {
		String eventStartDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		festaService.fetchAndSaveFestivals(eventStartDate, areaCode);
		return ResponseEntity.ok("축제 데이터 수집 및 저장 완료");
	}

}