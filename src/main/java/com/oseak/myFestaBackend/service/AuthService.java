package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.util.JwtUtil;
import com.oseak.myFestaBackend.domain.CustomUserDetails;
import com.oseak.myFestaBackend.domain.Member;
import com.oseak.myFestaBackend.dto.auth.LoginRequestDto;
import com.oseak.myFestaBackend.dto.auth.LoginResponseDto;
import com.oseak.myFestaBackend.dto.auth.RefreshTokenRequestDto;
import com.oseak.myFestaBackend.dto.auth.TokenResponseDto;
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

	public LoginResponseDto login(LoginRequestDto request) {
		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		Member member = userDetails.getMember();

		if (member.getProvider() != Member.Provider.local) {
			throw new OsaekException(AUTH_LOGIN_METHOD_INVALID);
		}

		String accessToken = jwtUtil.generateAccessToken(member);
		String refreshToken = jwtUtil.generateRefreshToken(member);

		LoginResponseDto response = LoginResponseDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.email(member.getEmail())
			.nickname(member.getNickname())
			.memberId(member.getId())
			.build();

		log.info("User logged in successfully : {}", member.getEmail());
		return response;
	}

	public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {

		String refreshToken = request.getRefreshToken();
		Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);

		Member member = memberRepository.findByIdAndIsWithdrawnIsFalse(memberId)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));

		String newAccessToken = jwtUtil.generateAccessToken(member);

		return TokenResponseDto.builder()
			.accessToken(newAccessToken)
			.build();
	}
}
