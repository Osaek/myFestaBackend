package com.oseak.myFestaBackend.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {
	private String accessToken;
	private String refreshToken;
	private Long memberId;
	private String nickname;
	private String email;
	private String profile;

	@Builder
	public LoginResponseDto(String accessToken, String refreshToken, Long
		memberId, String nickname, String email, String profile) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.memberId = memberId;
		this.nickname = nickname;
		this.email = email;
		this.profile = profile;
	}
}
