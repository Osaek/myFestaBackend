package com.oseak.myFestaBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.response.ApiResponse;
import com.oseak.myFestaBackend.dto.auth.LoginRequestDto;
import com.oseak.myFestaBackend.dto.auth.LoginResponseDto;
import com.oseak.myFestaBackend.dto.auth.RefreshTokenRequestDto;
import com.oseak.myFestaBackend.dto.auth.TokenResponseDto;
import com.oseak.myFestaBackend.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
		LoginResponseDto response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(@RequestBody RefreshTokenRequestDto request) {
		TokenResponseDto response = authService.refreshToken(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
		// TODO: 블랙리스트 방법으로 할거라면 구현해야함.
		// 현재는 서버에서는 아무것도 안하고 클라이언트 측 토큰 삭제 방법
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
