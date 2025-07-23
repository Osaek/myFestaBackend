package com.oseak.myFestaBackend.dto.member;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateUserRequestDto {

	// TODO: message를 다국어 처리
	@Pattern(
		regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
		message = "올바른 이메일 형식이 아닙니다."
	)
	@NotBlank(message = "이메일은 필수입니다.")
	@Size(max = 256, message = "이메일은 최대 {max}를 넘을 수 없습니다.")
	private String email;

	private String nickname;

	@NotBlank(message = "패스워드는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String password;

	@NotBlank(message = "패스워드 확인은 필수입니다.")
	private String passwordConfirm;

	@AssertTrue(message = "비밀번호가 일치하지 않습니다.")
	public boolean isPasswordValid() {
		return password != null && password.equals(passwordConfirm);
	}
	// TODO: 이미지 처리
	// private String profile;

}
