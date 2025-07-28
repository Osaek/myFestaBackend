package com.oseak.myFestaBackend.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트 요청에 대한 에러 코드를 정의합니다.
 *
 * <p>에러 코드는 다음 규칙을 따릅니다:</p>
 * <ul>
 *   <li>형식: <code>OSAEK-[도메인코드][에러번호]</code></li>
 *   <li><b>도메인코드 (2자리)</b>: 10 = 회원/인증 도메인</li>
 *   <li><b>에러번호 (3자리)</b>: 도메인 내 개별 에러 식별 번호</li>
 * </ul>
 *
 * 예: <code>OSAEK-10001</code> → 회원 도메인(10)에서 정의한 첫 번째 에러
 */
@Getter
@RequiredArgsConstructor
public enum ClientErrorCode implements BaseErrorCode {

	// 회원 조회 관련
	USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-10001", "user.id.not_found"),
	USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-10002", "user.email.not_found"),

	// 회원가입/중복 관련
	USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "OSAEK-10003", "user.email.duplicate"),
	USER_NICKNAME_GENERATION_ATTEMPT_EXCEEDED(HttpStatus.CONFLICT, "OSAEK-10004",
		"user.nickname.generation_attempt_exceeded"),

	// 로그인 관련
	AUTH_CREDENTIALS_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10005", "auth.credentials.invalid"),
	AUTH_ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "OSAEK-10006", "auth.account.disabled"),
	AUTH_ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN, "OSAEK-10007", "auth.account.withdrawn"),
	AUTH_LOGIN_METHOD_INVALID(HttpStatus.FORBIDDEN, "OSAEK-10007", "auth.login.method.invalid"),

	// 권한 관련
	USER_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "OSAEK-10008", "user.unauthorized"),

	// jwt 토큰 관련
	JWT_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10010", "jwt.refresh_token.invalid"),
	JWT_TOKEN_RETIRED(HttpStatus.UNAUTHORIZED, "OSAEK-10011", "jwt.token.retired"),
	JWT_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10012", "jwt.token.invalid"),
	JWT_TOKEN_SIGN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10013", "jwt.token.sign_invalid"),
	JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "OSAEK-10014", "jwt.token.not_found"),

	// 패스워드 관련
	PASSWORD_NOT_FOUND(HttpStatus.UNAUTHORIZED, "OSAEK-10015", "password.not_found"),
	PASSWORD_NOT_CORRECT(HttpStatus.UNAUTHORIZED, "OSAEK-10016", "password.not_correct"),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
