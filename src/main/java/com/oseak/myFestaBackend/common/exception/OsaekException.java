package com.oseak.myFestaBackend.common.exception;

import com.oseak.myFestaBackend.common.exception.code.BaseErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Osaek 프로젝트의 전역 커스텀 예외 클래스입니다.
 *
 * <p>비즈니스 로직에서 발생할 수 있는 예외를 표현하며,
 * {@link BaseErrorCode}를 기반으로 에러 코드, 메시지 키, HTTP 상태를 함께 전달합니다.</p>
 *
 * <p>다국어 메시지는 {@code messageSource.getMessage(errorCode.getMessageKey())}
 * 방식으로 별도 처리되며, 예외 자체에는 메시지 문자열을 포함하지 않습니다.</p>
 *
 * <pre>
 * 예시:
 * throw new OsaekException(UserErrorCode.USER_ID_NOT_FOUND_404);
 * </pre>
 */
@Schema(description = "공통 커스텀 에외 클래스")
@Getter
public class OsaekException extends RuntimeException {

	/**
	 * 비즈니스 예외의 정의를 담고 있는 에러 코드
	 */
	private final BaseErrorCode errorCode;

	/**
	 * 기본 생성자 - cause 없이 에러 코드만 전달
	 *
	 * @param errorCode 에러 코드 enum (예: USER_ID_NOT_FOUND_404)
	 */
	public OsaekException(BaseErrorCode errorCode) {
		super();
		this.errorCode = errorCode;
	}

	/**
	 * 원인 예외 포함 생성자
	 *
	 * @param errorCode 에러 코드 enum
	 * @param cause     예외 원인
	 */
	public OsaekException(BaseErrorCode errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}
}