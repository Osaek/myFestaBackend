package com.oseak.myFestaBackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oseak.myFestaBackend.common.response.ApiResponse;
import com.oseak.myFestaBackend.common.util.SecurityUtil;
import com.oseak.myFestaBackend.dto.member.ChangePasswordRequestDto;
import com.oseak.myFestaBackend.dto.member.ChangePasswordResponseDto;
import com.oseak.myFestaBackend.dto.member.CreateUserRequestDto;
import com.oseak.myFestaBackend.dto.member.CreateUserResponseDto;
import com.oseak.myFestaBackend.dto.member.WithdrawMemberResponseDto;
import com.oseak.myFestaBackend.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<CreateUserResponseDto>> createMember(
		@RequestBody @Valid CreateUserRequestDto request) {
		CreateUserResponseDto user = memberService.createUser(request);

		return ResponseEntity.ok(ApiResponse.success(user));
	}

	@PostMapping("/withdraw")
	public ResponseEntity<ApiResponse<WithdrawMemberResponseDto>> withdrawMember() {
		Long memberId = SecurityUtil.getCurrentUserId();
		WithdrawMemberResponseDto response = memberService.withdrawMember(memberId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/change")
	public ResponseEntity<ApiResponse<ChangePasswordResponseDto>> changePassword(
		@RequestBody @Valid ChangePasswordRequestDto request) {
		Long memberId = SecurityUtil.getCurrentUserId();

		ChangePasswordResponseDto response = memberService.changePassword(request, memberId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
