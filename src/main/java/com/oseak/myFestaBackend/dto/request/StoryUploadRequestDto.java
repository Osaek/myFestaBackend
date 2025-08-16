package com.oseak.myFestaBackend.dto.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스토리 업로드 요청")
public class StoryUploadRequestDto {
	@Schema(description = "업로드할 파일", required = true)
	private MultipartFile file;

	@Schema(description = "회원 ID", example = "1", required = true)
	private Long memberId;

	@Schema(description = "축제 ID", example = "140930", required = true)
	private Long festaId;

	@Schema(description = "축제 이름", example = "탐라문화제", required = true)
	private String festaName;

	@Schema(description = "공개 여부", example = "true", defaultValue = "true")
	@Builder.Default
	private Boolean isOpen = true;
}
