package com.oseak.myFestaBackend.dto;

import java.util.List;

import com.oseak.myFestaBackend.entity.enums.MediaType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThumbnailResult {
	private MediaType mediaType;
	private List<Thumbnail> thumbnails;
	private String originalLocalPath;
	private String staticThumbnailS3Url;    // 비디오용 정적 썸네일 S3 URL
	private String compressedOriginalPath;  // 압축된 원본 파일 로컬 경로 (임시)
}
