package com.oseak.myFestaBackend.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.util.JwtUtil;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.enums.Provider;
import com.oseak.myFestaBackend.service.MemberDetailsService;

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

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			final String authorizationHeader = request.getHeader("Authorization");
			String jwt = null;

			// Authorization 헤더에서 JWT 토큰 추출
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = jwtUtil.resolveToken(authorizationHeader);

				if (jwt != null && jwtUtil.validateToken(jwt) && jwtUtil.isAccessToken(jwt)) {
					String email = jwtUtil.getEmailFromToken(jwt);
					Long memberId = jwtUtil.getMemberIdFromToken(jwt);
					String nickname = jwtUtil.getNicknameFromToken(jwt);
					Provider provider = jwtUtil.getProviderFromToken(jwt);

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

					log.debug("JWT authentication successful for user: {} (ID: {})", email, memberId);
				} else {
					log.warn("JWT token validation failed for request: {}", request.getRequestURI());
				}
			} else {
				log.debug("No Authorization header found for request: {}", request.getRequestURI());
			}

			filterChain.doFilter(request, response);
		} catch (OsaekException ex) {
			log.error("JWT authentication error: {} for request: {}", ex.getMessage(), request.getRequestURI());
			// 필터에서 발생한 OsaekExeption을 처리
			SecurityContextHolder.clearContext();

			// 예외 정보를 request 속성에 저장하여 다음 필터에서 사용할 수 있게 함
			request.setAttribute("exception", ex);

			// 필터체인 계속 진행 (DispatcherServlet에 도달하면 GlobalExceptionHandler가 처리)
			filterChain.doFilter(request, response);
		}
	}
}
