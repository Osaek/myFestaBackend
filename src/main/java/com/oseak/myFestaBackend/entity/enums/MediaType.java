package com.oseak.myFestaBackend.entity.enums;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.oseak.myFestaBackend.common.exception.OsaekException;

import lombok.Getter;

public enum MediaType {
	IMAGE("image", "이미지",
		Set.of("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"),
		Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff")),

	VIDEO("video", "영상",
		Set.of("video/mp4", "video/avi", "video/quicktime", "video/x-msvideo", "video/webm"),
		Set.of("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v"));
	
	@Getter
	private final String type;
	@Getter
	private final String description;
	private final Set<String> supportedContentTypes;
	private final Set<String> supportedExtensions;

	MediaType(String type, String description, Set<String> contentTypes, Set<String> extensions) {
		this.type = type;
		this.description = description;
		this.supportedContentTypes = contentTypes;
		this.supportedExtensions = extensions;
	}

	// MultipartFile에서 직접 MediaType 감지
	public static MediaType detectFromFile(MultipartFile file) {
		// 1. Content-Type 확인
		String contentType = file.getContentType();
		if (contentType != null) {
			for (MediaType mediaType : values()) {
				if (mediaType.supportedContentTypes.contains(contentType.toLowerCase())) {
					return mediaType;
				}
			}
		}

		// 2. 파일 확장자 확인
		String filename = file.getOriginalFilename();
		if (filename != null) {
			String extension = getFileExtension(filename).toLowerCase();
			for (MediaType mediaType : values()) {
				if (mediaType.supportedExtensions.contains(extension)) {
					return mediaType;
				}
			}
		}

		throw new OsaekException(UNSUPPORTED_MEDIA_TYPE);
	}

	private static String getFileExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		return lastDot > 0 ? filename.substring(lastDot + 1) : "";
	}

}
