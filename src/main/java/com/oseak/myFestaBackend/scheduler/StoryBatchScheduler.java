package com.oseak.myFestaBackend.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.service.StoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryBatchScheduler {
	private final StoryService storyService;

	@Scheduled(cron = "0 20 0 * * *", zone = "Asia/Seoul")
	public void deleteStoryBatch() {
		log.info("========== 스토리 삭제 시작 ==========");
		try {
			storyService.deleteStory();
		} catch (Exception e) {
			log.error("스토리 삭제 중 오류 발생", e);
		}
	}
}
