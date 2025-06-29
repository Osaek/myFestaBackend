package com.oseak.myFestaBackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.oseak.myFestaBackend.service.KakaoApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Kakao OAuth API", description = "카카오 OAuth 관련 API (KakaoController)")
@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoController {

	private final KakaoApiService kakaoApiService;

	@Operation(
		summary = "카카오 로그인 URL 리다이렉트",
		description = "카카오 OAuth 인증을 위한 로그인 URL로 리다이렉트합니다. 사용자는 해당 URL을 통해 카카오 로그인 인증을 수행하게 됩니다.",
		responses = {
			@ApiResponse(responseCode = "302", description = "카카오 로그인 URL로 리다이렉트"),
			@ApiResponse(responseCode = "500", description = "카카오 로그인 URL 생성 실패")
		}
	)
	@GetMapping("/user/oauth/kakao")
	public String redirectToKakao() {
		String kakaoLoginUrl = kakaoApiService.generateKakaoLoginUrl();
		return "redirect:" + kakaoLoginUrl;
	}

	//TODO : return 후 메인 페이지로 이동시킬 페이지 추가 필요
	@Operation(
		summary = "카카오 로그인 콜백",
		description = "카카오 인증 서버로부터 인가 코드(code)를 수신한 뒤, 로그인 처리를 수행합니다."
	)
	@GetMapping("/login/oauth2/code/kakao")
	public String kakaoCallback(@RequestParam("code") String code) throws Exception {
		String result = kakaoApiService.kakaoLoginProcess(code);
		log.info("카카오 로그인 처리 결과: {}", result);
		return "redirect:/";
	}
}
