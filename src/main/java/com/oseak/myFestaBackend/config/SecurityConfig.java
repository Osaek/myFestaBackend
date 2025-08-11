package com.oseak.myFestaBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ClientErrorCode;
import com.oseak.myFestaBackend.common.response.CommonResponse;
import com.oseak.myFestaBackend.config.filter.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity // 스프링 시큐리티를 통해서 관리된다.
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	// 암호화를 진행하기 위한 함수
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {

		return new BCryptPasswordEncoder(10);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http

			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/**").permitAll()
				.requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/", "/login", "/kakao/login-url").permitAll()
				.anyRequest().authenticated())
			.httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증도 비활성화

			.formLogin((form) -> form
				.loginPage("/login")
				.defaultSuccessUrl("/")
				.permitAll())

			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					// 이전 필터에서 저장한 OsaekException이 있는지 확인
					OsaekException ex = (OsaekException)request.getAttribute("exception");
					if (ex == null) {
						// 없으면 기본 인증 오류 생성
						ex = new OsaekException(ClientErrorCode.AUTH_CREDENTIALS_INVALID);
					}

					log.warn("Authentication failed: {}, URI: {}",
						ex.getMessage(), request.getRequestURI());

					// 응답 생성
					response.setStatus(ex.getErrorCode().getHttpStatus().value());
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");

					// ApiResponse 형식으로 오류 응답 생성
					ObjectMapper objectMapper = new ObjectMapper();
					CommonResponse<?> errorResponse = CommonResponse.fail(
						ex.getErrorCode().getCode(),
						ex.getErrorCode().getMessageKey() // 실제로는 MessageUtil을 통해 메시지를 가져와야 함
					);

					objectMapper.writeValue(response.getOutputStream(), errorResponse);
				}))

			// TODO: 실제 서비스 구동시 활성화
			.csrf(csrf -> csrf.disable());  // CSRF 비활성화

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
		Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
