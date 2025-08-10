package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.util.JwtUtil;
import com.oseak.myFestaBackend.dto.auth.LoginRequestDto;
import com.oseak.myFestaBackend.dto.auth.LoginResponseDto;
import com.oseak.myFestaBackend.dto.auth.RefreshTokenRequestDto;
import com.oseak.myFestaBackend.dto.auth.RefreshTokenResponseDto;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.MemberRefreshToken;
import com.oseak.myFestaBackend.entity.enums.Provider;
import com.oseak.myFestaBackend.repository.MemberRefreshTokenRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;
	private final MemberRefreshTokenRepository memberRefreshTokenRepository;

	public LoginResponseDto login(LoginRequestDto request) {
		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
		try {
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			Member member = userDetails.getMember();

			if (member.getProvider() != Provider.LOCAL) {
				throw new OsaekException(AUTH_LOGIN_METHOD_INVALID);
			}

			return createJwtToken(member);
		} catch (Exception e) {
			throw e;
		}
	}

	public RefreshTokenResponseDto refreshAccessToken(RefreshTokenRequestDto request) {

		String refreshToken = request.getRefreshToken();
		if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
			throw new OsaekException(JWT_REFRESH_TOKEN_INVALID);
		}

		Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);

		MemberRefreshToken storedToken = memberRefreshTokenRepository.findByMemberId(memberId)
			.orElseThrow(() -> new OsaekException(JWT_REFRESH_TOKEN_NOT_FOUND));

		if (!storedToken.getToken().equals(refreshToken)) {
			throw new OsaekException(JWT_REFRESH_TOKEN_INVALID);
		}

		Member member = memberRepository.findByIdAndIsWithdrawnIsFalse(memberId)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));

		String newAccessToken = jwtUtil.refreshAccessToken(refreshToken, memberId);

		return RefreshTokenResponseDto.builder()
			.accessToken(newAccessToken)
			.build();
	}

	private void saveOrUpdateRefreshToken(Long memberId, String refreshToken) {
		Optional<MemberRefreshToken> existingToken = memberRefreshTokenRepository.findByMemberId(memberId);

		if (existingToken.isPresent()) {
			MemberRefreshToken tokenEntity = existingToken.get();
			tokenEntity.updateToken(refreshToken);
			memberRefreshTokenRepository.save(tokenEntity);
		} else {
			MemberRefreshToken newToken = MemberRefreshToken.builder()
				.memberId(memberId)
				.token(refreshToken)
				.build();
			memberRefreshTokenRepository.save(newToken);
		}
	}

	@Transactional
	public LoginResponseDto createJwtToken(Member member) {
		String accessToken = jwtUtil.generateAccessToken(member);
		String refreshToken = jwtUtil.generateRefreshToken(member);

		saveOrUpdateRefreshToken(member.getId(), refreshToken);

		return LoginResponseDto.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.nickname(member.getNickname())
			.build();
	}

	public void logout(String token) {
		if (!jwtUtil.validateToken(token)) {
			throw new OsaekException(JWT_TOKEN_INVALID);
		}

		Long memberId = jwtUtil.getMemberIdFromToken(token);

		memberRefreshTokenRepository.deleteByMemberId(memberId);
	}
}
