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
import com.oseak.myFestaBackend.dto.auth.RefreshTokenResponseDto;
import com.oseak.myFestaBackend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "인증 관련 API - 로그인, 로그아웃, 토큰 갱신")
public class AuthController {
	private final AuthService authService;

	@Operation(
		summary = "일반 로그인",
		description = "이메일과 비밀번호를 사용한 일반 로그인을 수행합니다. LOCAL 제공자로 가입된 사용자만 로그인 가능합니다.",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "로그인 성공",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						name = "성공 응답",
						value = """
							{
							    "status": 200,
							    "message": "요청이 성공했습니다.",
							    "data": {
							        "memberId": 1,
							        "email": "user@example.com",
							        "nickname": "사용자",
							        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
							        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
							    }
							}
							"""
					)
				)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "400",
				description = "로그인 실패 - 잘못된 이메일 또는 비밀번호",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 400,
							    "code": "OSAEK-00001",
							    "message": "인증에 실패했습니다."
							}
							"""
					)
				)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "422",
				description = "OAuth 사용자가 일반 로그인 시도",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 422,
							    "code": "AUTH_LOGIN_METHOD_INVALID",
							    "message": "잘못된 로그인 방식입니다. 소셜 로그인을 이용해주세요."
							}
							"""
					)
				)
			)
		}
	)
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
		LoginResponseDto response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "액세스 토큰 갱신",
		description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "토큰 갱신 성공",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						name = "성공 응답",
						value = """
							{
							    "status": 200,
							    "message": "요청이 성공했습니다.",
							    "data": {
							        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
							    }
							}
							"""
					)
				)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "401",
				description = "유효하지 않은 Refresh Token",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 401,
							    "code": "JWT_REFRESH_TOKEN_INVALID",
							    "message": "유효하지 않은 리프레시 토큰입니다."
							}
							"""
					)
				)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "404",
				description = "Refresh Token을 찾을 수 없음",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 404,
							    "code": "JWT_REFRESH_TOKEN_NOT_FOUND",
							    "message": "리프레시 토큰을 찾을 수 없습니다."
							}
							"""
					)
				)
			)
		}
	)
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> refreshAccessToken(
		@RequestBody RefreshTokenRequestDto request) {
		RefreshTokenResponseDto response = authService.refreshAccessToken(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "로그아웃",
		description = "사용자 로그아웃을 수행합니다. 서버에 저장된 Refresh Token을 삭제하여 토큰 무효화를 진행합니다.",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "로그아웃 성공",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						name = "성공 응답",
						value = """
							{
							    "status": 200,
							    "message": "요청이 성공했습니다.",
							    "data": null
							}
							"""
					)
				)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "401",
				description = "유효하지 않은 Access Token",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 401,
							    "code": "JWT_TOKEN_INVALID",
							    "message": "유효하지 않은 토큰입니다."
							}
							"""
					)
				)
			)
		}
	)
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
		// TODO: 블랙리스트 방법으로 할거라면 구현해야함.
		// 현재는 서버에서는 아무것도 안하고 클라이언트 측 토큰 삭제 방법
		authService.logout(token);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
