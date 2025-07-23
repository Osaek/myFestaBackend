package com.oseak.myFestaBackend.dto.member;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {

	@NotBlank(message = "패스워드는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String currentPassword;

	@NotBlank(message = "패스워드는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String newPassword;

	@NotBlank(message = "패스워드는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String confirmPassword;

	@AssertTrue(message = "비밀번호가 일치하지 않습니다.")
	public boolean isPasswordValid() {
		return newPassword != null && newPassword.equals(confirmPassword);
	}

	@AssertTrue(message = "비밀번호가 이전 비밀번호와 동일합니다.")
	public boolean isDiffPasswordValid() {
		return !currentPassword.equals(newPassword);
	}
}
