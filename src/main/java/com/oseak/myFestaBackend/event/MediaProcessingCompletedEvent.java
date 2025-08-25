package com.oseak.myFestaBackend.event;

import com.oseak.myFestaBackend.entity.enums.ProcessingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MediaProcessingCompletedEvent {
    private final Long storyId;
    private final String originalS3Url;
    private final String thumbnailS3Url;
    private final ProcessingStatus status;
}