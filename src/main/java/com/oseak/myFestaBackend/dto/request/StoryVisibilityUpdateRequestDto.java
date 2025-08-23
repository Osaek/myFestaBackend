package com.oseak.myFestaBackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoryVisibilityUpdateRequestDto {

	@NotBlank(message = "{story.code.required}")
	@Schema(description = "스토리 코드", example = "1390147", required = true)
	private String storyCode;

	@NotNull(message = "{story.is_open.required}")
	@Schema(description = "공개 여부 목표 상태", example = "true", required = true)
	private Boolean isOpen;
}
