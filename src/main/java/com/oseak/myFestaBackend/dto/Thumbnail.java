package com.oseak.myFestaBackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Thumbnail {
	private String size;
	private String localPath;    // 임시 로컬 경로
	private String s3Url;        // S3 URL만 저장
	private String format;
	private Integer width;
	private Integer height;

	// 필요시 키 추출 메서드
	public String extractS3Key() {
		if (s3Url == null)
			return null;

		// URL에서 키 부분만 추출
		// https://bucket.s3.region.amazonaws.com/key → key
		String[] parts = s3Url.split(".com/", 2);
		return parts.length > 1 ? parts[1] : null;
	}
}
