package com.oseak.myFestaBackend.dto.member;

import com.oseak.myFestaBackend.domain.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateUserResponseDto {

	private String email;
	private String nickname;
	private Member.Provider provider;

	public CreateUserResponseDto(Member member) {
		this.email = member.getEmail();
		this.nickname = member.getNickname();
		this.provider = member.getProvider();
	}
}
