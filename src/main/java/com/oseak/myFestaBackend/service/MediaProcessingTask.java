package com.oseak.myFestaBackend.service;

import java.io.File;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.dto.Thumbnail;
import com.oseak.myFestaBackend.dto.ThumbnailResult;
import com.oseak.myFestaBackend.entity.enums.ProcessingStatus;
import com.oseak.myFestaBackend.event.MediaProcessingCompletedEvent;
import com.oseak.myFestaBackend.event.MediaProcessingEvent;
import com.oseak.myFestaBackend.generator.ThumbnailGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingTask {
    
    private final ThumbnailGenerator thumbnailGenerator;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;
    
    @EventListener
    @Async("mediaProcessingExecutor")
    public void handleMediaProcessing(MediaProcessingEvent event) {
        String tempMediaId = UUID.randomUUID().toString();
        
        try {
            log.info("Starting async media processing for storyId: {}, mediaType: {}", 
                    event.getStoryId(), event.getMediaType());
            
            // 1. 썸네일 생성
            ThumbnailResult result = thumbnailGenerator.generateThumbnails(
                    event.getFile(), tempMediaId, event.getMediaType());
            
            // 2. 결과 검증
            if (result == null || result.getThumbnails() == null || result.getThumbnails().isEmpty()) {
                log.error("Failed to generate thumbnails for storyId: {}", event.getStoryId());
                publishCompletedEvent(event.getStoryId(), null, null, ProcessingStatus.FAILED);
                return;
            }
            
            // 3. 첫 번째 썸네일 가져오기
            Thumbnail firstThumbnail = result.getThumbnails().get(0);
            if (firstThumbnail == null || firstThumbnail.getLocalPath() == null) {
                log.error("Invalid thumbnail for storyId: {}", event.getStoryId());
                publishCompletedEvent(event.getStoryId(), null, null, ProcessingStatus.FAILED);
                return;
            }
            
            // 4. 파일 존재 확인
            File thumbnailFile = new File(firstThumbnail.getLocalPath());
            if (!thumbnailFile.exists() || thumbnailFile.length() == 0) {
                log.error("Thumbnail file invalid for storyId: {}", event.getStoryId());
                publishCompletedEvent(event.getStoryId(), null, null, ProcessingStatus.FAILED);
                return;
            }
            
            // 5. 원본 파일 S3 업로드 (있는 경우)
            String originalS3Url = null;
            String originalLocalPath = result.getOriginalLocalPath();
            if (originalLocalPath != null && !originalLocalPath.trim().isEmpty()) {
                File originalFile = new File(originalLocalPath);
                if (originalFile.exists() && originalFile.length() > 0) {
                    originalS3Url = s3Service.uploadFile(originalFile);
                    originalFile.delete();
                    log.info("Uploaded original file to S3 for storyId: {}", event.getStoryId());
                }
            }
            
            // 6. 썸네일 S3 업로드
            String thumbnailS3Url = s3Service.uploadFile(thumbnailFile);
            thumbnailFile.delete();
            log.info("Uploaded thumbnail to S3 for storyId: {}", event.getStoryId());
            
            // 7. 완료 이벤트 발행
            publishCompletedEvent(event.getStoryId(), originalS3Url, thumbnailS3Url, ProcessingStatus.COMPLETED);
            
        } catch (Exception e) {
            log.error("Error processing media for storyId: {}", event.getStoryId(), e);
            publishCompletedEvent(event.getStoryId(), null, null, ProcessingStatus.FAILED);
        }
    }
    
    private void publishCompletedEvent(Long storyId, String originalS3Url, String thumbnailS3Url, ProcessingStatus status) {
        eventPublisher.publishEvent(new MediaProcessingCompletedEvent(storyId, originalS3Url, thumbnailS3Url, status));
    }
}