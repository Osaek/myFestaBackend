package com.oseak.myFestaBackend.common.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.BaseErrorCode;
import com.oseak.myFestaBackend.common.exception.code.CommonErrorCode;
import com.oseak.myFestaBackend.common.exception.util.MessageUtil;
import com.oseak.myFestaBackend.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

/**
 * 전역 예외를 처리하는 핸들러 클래스입니다.
 *
 * <p>컨트롤러 전반에서 발생하는 예외를 공통 포맷으로 처리하여
 * ApiResponse 형태로 반환합니다.</p>
 *
 */
@Schema(name = "GlobalExceptionHandler", description = "전역 예외 처리 핸들러")
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private final MessageUtil messageUtil;

	/**
	 * {@link OsaekException} 예외를 처리하는 핸들러입니다.
	 *
	 * <p>비즈니스 로직 중 발생하는 커스텀 예외를 전역에서 처리하며,
	 * 에러 코드에 포함된 메시지 키를 기반으로 로컬라이즈된 메시지를 추출하여
	 * 공통 응답 형식 {@link ApiResponse}로 변환하여 반환합니다.</p>
	 *
	 * @param ex OsaekException (BaseErrorCode를 포함하는 커스텀 예외)
	 * @return ApiResponse 형태의 HTTP 응답 (에러 메시지 포함)
	 */
	@ExceptionHandler(OsaekException.class)
	public ResponseEntity<ApiResponse<Void>> handleOsaekException(OsaekException ex) {
		BaseErrorCode errorCode = ex.getErrorCode();
		return buildErrorResponse(errorCode);
	}

	/**
	 * 처리되지 않은 모든 예외를 처리합니다.
	 *
	 * @param ex 발생한 예외
	 * @return HTTP 500 상태와 함께 표준 ApiResponse를 반환
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneralError(Exception ex) {
		CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR_500;

		return buildErrorResponse(
			errorCode,
			ex.getMessage()
		);
	}

	/**
	 * 공통 에러 응답을 생성합니다.
	 *
	 * @param errorCode 에러 코드 (BaseErrorCode 구현체)
	 * @return 응답 엔티티
	 */
	private ResponseEntity<ApiResponse<Void>> buildErrorResponse(BaseErrorCode errorCode) {
		String localizedMessage = messageUtil.getMessage(errorCode.getMessageKey());
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(
				errorCode.getHttpStatus(),
				errorCode.getCode(),
				localizedMessage
			));
	}

	/**
	 * 공통 에러 응답을 생성합니다.
	 *
	 * @param errorCode 에러 코드 (BaseErrorCode 구현체)
	 * @param customMessage 지정한 에러 메시지
	 * @return 응답 엔티티
	 */
	private ResponseEntity<ApiResponse<Void>> buildErrorResponse(BaseErrorCode errorCode, String customMessage) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(
				errorCode.getHttpStatus(),
				errorCode.getCode(),
				customMessage
			));
	}
}