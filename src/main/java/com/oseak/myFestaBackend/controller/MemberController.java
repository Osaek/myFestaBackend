package com.oseak.myFestaBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.common.util.SecurityUtil;
import com.oseak.myFestaBackend.dto.member.ChangePasswordRequestDto;
import com.oseak.myFestaBackend.dto.member.ChangePasswordResponseDto;
import com.oseak.myFestaBackend.dto.member.CreateUserRequestDto;
import com.oseak.myFestaBackend.dto.member.CreateUserResponseDto;
import com.oseak.myFestaBackend.dto.member.WithdrawMemberResponseDto;
import com.oseak.myFestaBackend.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@Operation(
		summary = "회원가입",
		description = "새로운 회원을 등록합니다. 이메일 중복 검사를 수행합니다.",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "회원가입 성공",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						name = "성공 응답",
						value = """
							{
							    "status": 201,
							    "message": "요청이 성공했습니다.",
							    "data": {
							        "memberId": 1,
							        "email": "newuser@example.com",
							        "nickname": "새로운사용자"
							    }
							}
							"""
					)
				)
			),
			@ApiResponse(
				responseCode = "409",
				description = "이메일 중복",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 409,
							    "code": "EMAIL_ALREADY_EXISTS",
							    "message": "이미 사용 중인 이메일입니다."
							}
							"""
					)
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "유효성 검사 실패",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 400,
							    "code": "VALIDATION_FAILED",
							    "message": "이메일 형식이 올바르지 않습니다."
							}
							"""
					)
				)
			)
		}
	)
	@PostMapping("/signup")
	public ResponseEntity<CommonResponse<CreateUserResponseDto>> createMember(
		@RequestBody @Valid CreateUserRequestDto request) {
		CreateUserResponseDto user = memberService.createUser(request);

		return ResponseEntity.ok(CommonResponse.success(user));
	}

	@Operation(
		summary = "회원 탈퇴",
		description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다. 실제 데이터 삭제가 아닌 논리적 삭제(isWithdrawn=true)를 수행합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "탈퇴 성공",
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
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자"
		)
	})
	@PostMapping("/withdraw")
	public ResponseEntity<CommonResponse<WithdrawMemberResponseDto>> withdrawMember() {
		Long memberId = SecurityUtil.getCurrentUserId();
		WithdrawMemberResponseDto response = memberService.withdrawMember(memberId);

		return ResponseEntity.ok(CommonResponse.success(response));
	}

	@Operation(
		summary = "비밀번호 변경",
		description = "현재 비밀번호를 확인한 후 새로운 비밀번호로 변경합니다. LOCAL 제공자 사용자만 가능합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "비밀번호 변경 성공",
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
			@ApiResponse(
				responseCode = "400",
				description = "현재 비밀번호 불일치",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 400,
							    "code": "PASSWORD_MISMATCH",
							    "message": "현재 비밀번호가 일치하지 않습니다."
							}
							"""
					)
				)
			),
			@ApiResponse(
				responseCode = "403",
				description = "OAuth 사용자는 비밀번호 변경 불가",
				content = @Content(
					mediaType = "application/json",
					examples = @ExampleObject(
						value = """
							{
							    "status": 403,
							    "code": "OPERATION_NOT_ALLOWED",
							    "message": "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."
							}
							"""
					)
				)
			)
		}
	)
	@PostMapping("/change")
	public ResponseEntity<CommonResponse<ChangePasswordResponseDto>> changePassword(
		@RequestBody @Valid ChangePasswordRequestDto request) {
		Long memberId = SecurityUtil.getCurrentUserId();

		ChangePasswordResponseDto response = memberService.changePassword(request, memberId);
		return ResponseEntity.ok(CommonResponse.success(response));
	}
}
