package com.oseak.myFestaBackend.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 도메인의 에러 코드를 정의합니다.
 * 에러코드는 [도메인]_[속성/행위]_[상태]-[HTTP코드] 형식으로 구성됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

	// 회원 조회 관련
	USER_ID_NOT_FOUND_404(HttpStatus.NOT_FOUND, "USER_ID_NOT_FOUND-404", "user.id.not_found"),
	USER_EMAIL_NOT_FOUND_404(HttpStatus.NOT_FOUND, "USER_EMAIL_NOT_FOUND-404", "user.email.not_found"),

	// 회원가입/중복 관련
	USER_EMAIL_DUPLICATE_409(HttpStatus.CONFLICT, "USER_EMAIL_DUPLICATE-409", "user.email.duplicate"),
	USER_NICKNAME_DUPLICATE_409(HttpStatus.CONFLICT, "USER_NICKNAME_DUPLICATE-409", "user.nickname.duplicate"),

	// 로그인 관련
	AUTH_CREDENTIALS_INVALID_401(HttpStatus.UNAUTHORIZED, "AUTH_CREDENTIALS_INVALID-401", "auth.credentials.invalid"),
	AUTH_ACCOUNT_DISABLED_403(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_DISABLED-403", "auth.account.disabled"),
	AUTH_ACCOUNT_WITHDRAWN_403(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_WITHDRAWN-403", "auth.account.withdrawn"),

	// 권한 관련
	USER_UNAUTHORIZED_ACCESS_403(HttpStatus.FORBIDDEN, "USER_UNAUTHORIZED_ACCESS-403", "user.unauthorized");

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
