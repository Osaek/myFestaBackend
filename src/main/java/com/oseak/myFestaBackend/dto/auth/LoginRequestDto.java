package com.oseak.myFestaBackend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {

	@Pattern(
		regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
		message = "{validation.email.pattern}"
	)
	@NotBlank(message = "{validation.email.required}")
	@Size(max = 256, message = "{validation.email.size}")
	private String email;
	private String password;
}
