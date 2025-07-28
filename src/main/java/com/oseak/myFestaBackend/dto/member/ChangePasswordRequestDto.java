package com.oseak.myFestaBackend.dto.member;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {

	@NotBlank(message = "{validation.password.required}")
	@Size(min = 8, message = "{validation.password.size}")
	private String currentPassword;

	@NotBlank(message = "{validation.password.required}")
	@Size(min = 8, message = "{validation.password.size}")
	private String newPassword;

	@NotBlank(message = "{validation.password.required}")
	@Size(min = 8, message = "{validation.password.size}")
	private String confirmPassword;

	@AssertTrue(message = "{validation.password.mismatch}")
	public boolean isPasswordValid() {
		return newPassword != null && newPassword.equals(confirmPassword);
	}

	@AssertTrue(message = "{validation.password.same}")
	public boolean isDiffPasswordValid() {
		return !currentPassword.equals(newPassword);
	}
}
