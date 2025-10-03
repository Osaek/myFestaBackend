package com.oseak.myFestaBackend.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ClientErrorCode;
import com.oseak.myFestaBackend.common.util.JwtUtil;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.MemberRefreshToken;
import com.oseak.myFestaBackend.entity.enums.Provider;
import com.oseak.myFestaBackend.repository.MemberRefreshTokenRepository;
import com.oseak.myFestaBackend.service.MemberDetailsService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final MemberDetailsService memberDetailsService;
	private final MemberRefreshTokenRepository memberRefreshTokenRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			final String authorizationHeader = request.getHeader("Authorization");
			String jwt = null;

			log.debug("Processing request: {} with Authorization header: {}",
				request.getRequestURI(), authorizationHeader != null ? "present" : "missing");

			// Authorization 헤더에서 JWT 토큰 추출
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = jwtUtil.resolveToken(authorizationHeader);
				log.debug("Extracted JWT token: {}", jwt != null ? "valid" : "null");

				if (jwt != null && jwtUtil.isAccessToken(jwt)) {
					if (jwtUtil.validateToken(jwt)) {
						// 유효한 토큰 - 기존 로직
						authenticateUser(request, jwt);
						log.debug("JWT authentication successful for request: {}", request.getRequestURI());
					} else {
						// 만료된 토큰 - 자동 재발급 시도
						String newAccessToken = attemptTokenRefresh(jwt);
						if (newAccessToken != null) {
							authenticateUser(request, jwt, newAccessToken);
							response.setHeader("Authorization", "Bearer " + newAccessToken);
							response.setHeader("Token-Refreshed", "true");
							log.info("Access token automatically refreshed for request: {}", request.getRequestURI());
						} else {
							log.warn("JWT token refresh failed for request: {}", request.getRequestURI());
						}
					}
				} else if (jwt != null) {
					log.warn("Invalid token type for request: {}", request.getRequestURI());
				}
			} else {
				log.debug("No Authorization header found for request: {}", request.getRequestURI());
			}

			filterChain.doFilter(request, response);
		} catch (OsaekException ex) {
			log.error("JWT authentication error: {} for request: {}", ex.getMessage(), request.getRequestURI());
			handleJwtException(request, response, filterChain, ex);
		} catch (Exception ex) {
			log.error("Unexpected error in JWT filter", ex);
			handleJwtException(request, response, filterChain, new OsaekException(ClientErrorCode.JWT_TOKEN_INVALID));
		}
	}

	// 기존 authenticateUser 메서드
	private void authenticateUser(HttpServletRequest request, String jwt) {

		// 한 번만 파싱해서 모든 정보 추출
		Claims claims = jwtUtil.getAllClaimsFromToken(jwt);

		String email = claims.get("email", String.class);
		Long memberId = Long.parseLong(claims.getSubject());
		String nickname = claims.get("nickname", String.class);
		Provider provider = Provider.valueOf(claims.get("provider", String.class));

		log.debug("Authenticating user - memberId: {}, email: {}, nickname: {}", memberId, email, nickname);

		Member member = Member.builder()
			.email(email)
			.nickname(nickname)
			.provider(provider)
			.id(memberId)
			.build();

		UserDetails userDetails = new CustomUserDetails(member, null);
		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	private void authenticateUser(HttpServletRequest request, String oldJwt, String newJwt) {
		// 만료된 토큰에서 Member 정보 추출 (이미 한번 파싱했으므로 재사용)
		String email = jwtUtil.getEmailFromToken(oldJwt);
		Long memberId = jwtUtil.getMemberIdFromToken(oldJwt);
		String nickname = jwtUtil.getNicknameFromToken(oldJwt);
		Provider provider = jwtUtil.getProviderFromToken(oldJwt);

		// 보안 검증: 새 토큰과 기존 토큰의 memberId 일치 확인 (선택사항)
		try {
			Long newMemberId = jwtUtil.getMemberIdFromToken(newJwt);
			if (!memberId.equals(newMemberId)) {
				log.error("Token memberId mismatch during refresh: old={}, new={}", memberId, newMemberId);
				throw new OsaekException(ClientErrorCode.JWT_TOKEN_INVALID);
			}
		} catch (Exception e) {
			log.warn("Could not validate new token memberId: {}", e.getMessage());
		}

		Member member = Member.builder()
			.email(email)
			.nickname(nickname)
			.provider(provider)
			.id(memberId)
			.build();

		UserDetails userDetails = new CustomUserDetails(member, null);

		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		log.debug("Authentication completed using refreshed token for member: {}", memberId);
	}

	// 자동 토큰 재발급 메서드
	private String attemptTokenRefresh(String expiredAccessToken) {
		try {
			Long memberId = jwtUtil.getMemberIdFromToken(expiredAccessToken);

			MemberRefreshToken refreshTokenEntity = memberRefreshTokenRepository
				.findByMemberId(memberId).orElse(null);

			if (refreshTokenEntity != null &&
				jwtUtil.validateToken(refreshTokenEntity.getToken()) &&
				jwtUtil.isRefreshToken(refreshTokenEntity.getToken())) {

				String newAccessToken = jwtUtil.refreshAccessToken(refreshTokenEntity.getToken(), memberId);
				log.debug("Successfully refreshed access token for member: {}", memberId);
				return newAccessToken;
			}
		} catch (Exception e) {
			log.debug("Token refresh failed: {}", e.getMessage());
		}
		return null;
	}

	// JWT 예외 처리 메서드
	private void handleJwtException(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain, OsaekException ex) throws ServletException, IOException {

		SecurityContextHolder.clearContext();
		request.setAttribute("exception", ex);
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		// JWT 처리가 필요없는 경로들
		return path.startsWith("/auth/login") ||
			path.startsWith("/auth/refresh") ||
			path.startsWith("/member/signup") ||
			path.startsWith("/user/oauth/kakao") ||
			path.startsWith("/login/oauth2/") ||
			path.startsWith("/kakao/token") ||
			path.startsWith("/swagger") ||
			path.startsWith("/v3/api-docs") ||
			path.equals("/") ||
			path.equals("/error") ||
			path.equals("/favicon.ico");
	}
}
