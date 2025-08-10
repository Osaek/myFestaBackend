package com.oseak.myFestaBackend.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenResponseDto {
	private String accessToken;

	@Builder
	public RefreshTokenResponseDto(String accessToken) {
		this.accessToken = accessToken;
	}
}
