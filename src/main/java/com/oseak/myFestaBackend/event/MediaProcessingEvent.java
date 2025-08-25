package com.oseak.myFestaBackend.event;

import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.entity.enums.MediaType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MediaProcessingEvent {
    private final MultipartFile file;
    private final Long storyId;
    private final MediaType mediaType;
}