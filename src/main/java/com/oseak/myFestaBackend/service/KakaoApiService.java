package com.oseak.myFestaBackend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.MemberOauthToken;
import com.oseak.myFestaBackend.repository.MemberOauthTokenRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoApiService {

	private final MemberRepository memberRepository;
	private final MemberOauthTokenRepository oauthTokenRepository;
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

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

	public Map<String, Object> getKakaoToken(String code) {
		String response;
		try {
			response = webClient.post()
				.uri("https://kauth.kakao.com/oauth/token")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.body(BodyInserters.fromFormData("grant_type", "authorization_code")
					.with("client_id", clientId)
					.with("client_secret", clientSecret)
					.with("redirect_uri", redirectUri)
					.with("code", code))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new OsaekException(ServerErrorCode.TOKEN_REQUEST_FAILED);
		}

		try {
			return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonProcessingException e) {
			throw new OsaekException(ServerErrorCode.INVALID_JSON);
		}
	}

	public Map<String, Object> getUserInfo(String accessToken) {
		String response;
		try {
			response = webClient.get()
				.uri("https://kapi.kakao.com/v2/user/me")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new OsaekException(ServerErrorCode.USER_INFO_REQUEST_FAILED);
		}

		try {
			return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonProcessingException e) {
			throw new OsaekException(ServerErrorCode.INVALID_JSON);
		}
	}

	//TODO : 결과 확인을 위해 string으로 반환 추후에 void로 변환
	@Transactional
	public String kakaoLoginProcess(String code) {
		Map<String, Object> tokenMap = getKakaoToken(code);
		String accessToken = (String)tokenMap.get("access_token");
		String refreshToken = (String)tokenMap.get("refresh_token");

		long expiresIn = Long.parseLong(tokenMap.get("expires_in").toString());
		long refreshExpiresIn = Long.parseLong(tokenMap.get("refresh_token_expires_in").toString());

		LocalDateTime accessExpiresAt = LocalDateTime.ofInstant(
			Instant.now().plusSeconds(expiresIn), ZoneId.systemDefault());
		LocalDateTime refreshExpiresAt = LocalDateTime.ofInstant(
			Instant.now().plusSeconds(refreshExpiresIn), ZoneId.systemDefault());

		Map<String, Object> userInfo = getUserInfo(accessToken);

		Map<String, Object> kakaoAccount;
		Map<String, Object> profile;

		try {
			kakaoAccount = (Map<String, Object>)userInfo.get("kakao_account");
			profile = (Map<String, Object>)kakaoAccount.get("profile");
		} catch (Exception e) {
			throw new OsaekException(ServerErrorCode.MALFORMED_RESPONSE);
		}

		String email = (String)kakaoAccount.get("email");
		String nickname = (String)profile.get("nickname");
		String profileImage = (String)profile.get("profile_image_url");

		if (email == null || nickname == null) {
			throw new OsaekException(ServerErrorCode.MISSING_REQUIRED_FIELD);
		}

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

	private void saveOauthToken(Long memberId, String accessToken, String refreshToken, LocalDateTime expiresAt,
		LocalDateTime refreshTokenExpiresAt) {
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