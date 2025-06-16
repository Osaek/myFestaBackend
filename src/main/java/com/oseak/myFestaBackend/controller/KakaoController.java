package com.oseak.myFestaBackend.controller;

import com.oseak.myFestaBackend.service.KakaoApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoApiService kakaoApiService;

    @GetMapping("/kakao/login-url")
    public String redirectToKakao() {
        String kakaoLoginUrl = kakaoApiService.generateKakaoLoginUrl();
        return "redirect:" + kakaoLoginUrl;
    }

    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoCallback(@RequestParam("code") String code) throws Exception {
        String result = kakaoApiService.kakaoLoginProcess(code);
        System.out.println(result);
        return "redirect:/"; //여기에 나중에 우리 이동할 페이지 넣기
    }
}
