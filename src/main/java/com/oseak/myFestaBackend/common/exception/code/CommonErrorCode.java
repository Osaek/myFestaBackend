package com.oseak.myFestaBackend.common.exception.code;
  
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공통 에러 코드를 정의합니다.
 *
 * 에러코드는 [도메인]_[속성/행위]_[상태]-[HTTP코드] 형식으로 구성됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {
	INTERNAL_SERVER_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR-500", "server.internal_error"),
	SERVICE_UNAVAILABLE_503(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE-503", "server.unavailable");

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
