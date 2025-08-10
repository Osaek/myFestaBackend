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
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "OSAEK-00003", "server.unavailable"),
	INVALID_JSON(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-00004", "server.invalid_json"),
	MALFORMED_RESPONSE(HttpStatus.BAD_GATEWAY, "OSAEK-00005", "server.malformed_response"),
	MISSING_REQUIRED_FIELD(HttpStatus.BAD_GATEWAY, "OSAEK-00006", "server.missing_required_field"),
	OAUTH_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "OSAEK-00007", "server.oauth_provider_error"),
	TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "OSAEK-00008", "server.token_request_failed"),
	USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "OSAEK-00009", "server.user_info_request_failed"),
	FESTA_STATUS_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-00010", "server.festa_status_update_failed"),
	FESTA_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-00011", "server.festa_fetch_failed"),
	FESTA_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-00012", "server.festa_not_found");

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
