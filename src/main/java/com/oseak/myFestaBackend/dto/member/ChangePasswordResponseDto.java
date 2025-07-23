package com.oseak.myFestaBackend.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordResponseDto {

	private String message;

	@Builder
	public ChangePasswordResponseDto(String message) {
		this.message = message;
	}
}
