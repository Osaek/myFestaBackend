package com.oseak.myFestaBackend.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.repository.AreaRepository;
import com.oseak.myFestaBackend.service.FestaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestaBatchScheduler {

	private final FestaService festaService;
	private final AreaRepository areaRepository;

	@Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
	public void fetchAndSaveFestasBatch() {
		log.info("축제 정보 수집 배치 정상 시작");
		String eventStartDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		areaRepository.findAll().forEach(area -> {
			Integer areaCode = area.getAreaCode();
			try {
				log.info("지역 코드 {} 축제 수집 시작", areaCode);
				festaService.fetchAndSaveFestas(eventStartDate, areaCode);
			} catch (Exception e) {
				log.error("지역코드 {} 수집 중 오류 발생", areaCode, e);
				throw new OsaekException(ServerErrorCode.FESTA_FETCH_FAILED);
			}
		});
		log.info("축제 정보 수집 완료");
	}

	@Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
	public void updateFestaStatusBatch() {
		log.info("축제 상태 업데이트 시작");
		try {
			festaService.updateAllFestaStatus();
			log.info("축제 상태 업데이트 완료");
		} catch (Exception e) {
			log.error("축제 상태 업데이트 중 오류 발생", e);
			throw new OsaekException(ServerErrorCode.FESTA_STATUS_UPDATE_FAILED);
		}
	}
}
