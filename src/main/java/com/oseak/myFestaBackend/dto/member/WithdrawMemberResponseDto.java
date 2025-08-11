package com.oseak.myFestaBackend.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawMemberResponseDto {

	String email;
	String nickname;

	@Builder
	public WithdrawMemberResponseDto(String email, String nickname) {
		this.email = email;
		this.nickname = nickname;
	}
}
