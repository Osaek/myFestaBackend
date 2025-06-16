package com.oseak.myFestaBackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oseak.myFestaBackend.domain.Member;
import com.oseak.myFestaBackend.domain.MemberOauthToken;
import com.oseak.myFestaBackend.repository.MemberOauthTokenRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoApiService {

    private final MemberRepository memberRepository;
    private final MemberOauthTokenRepository oauthTokenRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public String generateKakaoLoginUrl() {
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=profile_nickname,account_email,profile_image",
                clientId, redirectUri
        );
    }

    public Map<String, Object> getKakaoToken(String code) throws Exception {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        String response = restTemplate.postForObject(tokenUrl, request, String.class);

        return objectMapper.readValue(response, Map.class);
    }

    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        String response = restTemplate.exchange(userInfoUrl, org.springframework.http.HttpMethod.GET, request, String.class).getBody();

        return objectMapper.readValue(response, Map.class);
    }


    //제대로 되는지 String으로 반환한거라 나중에 바꾸면 될 듯
    @Transactional
    public String kakaoLoginProcess(String code) throws Exception {
        Map<String, Object> tokenMap = getKakaoToken(code);
        String accessToken = (String) tokenMap.get("access_token");
        String refreshToken = (String) tokenMap.get("refresh_token");

        long expiresIn = Long.parseLong(tokenMap.get("expires_in").toString());
        long refreshExpiresIn = Long.parseLong(tokenMap.get("refresh_token_expires_in").toString());

        LocalDateTime accessExpiresAt = LocalDateTime.ofInstant(Instant.now().plusSeconds(expiresIn), ZoneId.systemDefault());
        LocalDateTime refreshExpiresAt = LocalDateTime.ofInstant(Instant.now().plusSeconds(refreshExpiresIn), ZoneId.systemDefault());

        Map<String, Object> userInfo = getUserInfo(accessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        Optional<Member> existingMember = memberRepository.findByEmail(email);
        if (existingMember.isPresent()) {
            saveOauthToken(existingMember.get().getId(), accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
            return "로그인 성공";
        }

        Member newMember = Member.builder()
                .email(email)
                .nickname(nickname)
                .profile(profileImage)
                .provider(Member.Provider.kakao)
                .build();

        memberRepository.save(newMember);
        saveOauthToken(newMember.getId(), accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);

        return "회원가입 성공";
    }

    private void saveOauthToken(Long memberId, String accessToken, String refreshToken, LocalDateTime expiresAt, LocalDateTime refreshTokenExpiresAt) {
        MemberOauthToken oauthToken = MemberOauthToken.builder()
                .memberId(memberId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .build();

        oauthTokenRepository.save(oauthToken);
    }
}
