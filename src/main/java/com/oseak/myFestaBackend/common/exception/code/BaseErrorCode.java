package com.oseak.myFestaBackend.common.exception.code;

import org.springframework.http.HttpStatus;

/**
 * 모든 에러 코드 enum이 구현해야 하는 공통 인터페이스입니다.
 * - HTTP 상태 코드
 * - 커스텀 코드 (예: OSAEK-00001)
 * - 사용자 메시지
 */
public interface BaseErrorCode {

	/**
	 * 해당 에러에 대한 HTTP 상태코드
	 */
	HttpStatus getHttpStatus();

	/**
	 * 비즈니스 용 커스텀 에러 코드 (예: OSAEK-00001)
	 */
	String getCode();

	/**
	 * 사용자에게 표시할 메시지의 다국어 메시지 키
	 */
	String getMessageKey();

}
