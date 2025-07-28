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
	
	@Pattern(
		regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
		message = "{validation.email.pattern}"
	)
	@NotBlank(message = "{validation.email.required}")
	@Size(max = 256, message = "{validation.email.size}")
	private String email;

	private String nickname;

	@NotBlank(message = "{validation.password.required}")
	@Size(min = 8, message = "{validation.password.size}")
	private String password;

	@NotBlank(message = "{validation.password.confirm.required}")
	private String passwordConfirm;

	@AssertTrue(message = "{validation.password.mismatch}")
	public boolean isPasswordValid() {
		return password != null && password.equals(passwordConfirm);
	}
	// TODO: 이미지 처리
	// private String profile;

}
