package com.oseak.myFestaBackend.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 서버 관련 에러 코드를 정의합니다.
 *
 * <p>에러 코드는 다음과 같은 규칙을 따릅니다:</p>
 * <ul>
 *   <li>형식: <code>OSAEK-[도메인코드][에러번호]</code></li>
 *   <li><b>도메인코드 (2자리)</b>: 00 = 서버 도메인</li>
 *   <li><b>에러번호 (3자리)</b>: 각 도메인 내에서 정의한 고유 번호</li>
 * </ul>
 *
 * 예: <code>OSAEK-00001</code> → 서버 도메인(00)에서 정의한 첫 번째 에러(001)
 */
@Getter
@RequiredArgsConstructor
public enum ServerErrorCode implements BaseErrorCode {
	UNKNOWN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-00001", "server.unknown_error"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-00002", "server.internal_error"),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "OSAEK-00003", "server.unavailable");

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
