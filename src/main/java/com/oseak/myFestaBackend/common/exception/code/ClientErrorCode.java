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
 * <p>
 * <code>OSAEK-10001</code> → 회원 도메인(10)에서 정의한 첫 번째 에러
 * <code>OSAEK-30001</code> → 축제 도메인(30)에서 정의한 첫 번째 에러
 */
@Getter
@RequiredArgsConstructor
public enum ClientErrorCode implements BaseErrorCode {
	// 검증메시지 관련
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "OSAEK-20001", "validation.invalid_input_value"),
	// ShortCode관련
	SHORT_CODE_INVALID(HttpStatus.BAD_REQUEST, "OSAEK-20002", "short.code.invalid"),
	// 권한 없음
	FORBIDDEN(HttpStatus.FORBIDDEN, "OSAEK-20003", "validation.forbidden"),
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
	JWT_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10110", "jwt.refresh_token.invalid"),
	JWT_REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-10111", "jwt.refresh_token.not_found"),
	JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.NOT_ACCEPTABLE, "OSAEK-10112", "jwt.refresh_token.expired"),
	JWT_TOKEN_RETIRED(HttpStatus.UNAUTHORIZED, "OSAEK-10113", "jwt.token.retired"),
	JWT_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10114", "jwt.token.invalid"),
	JWT_TOKEN_SIGN_INVALID(HttpStatus.UNAUTHORIZED, "OSAEK-10115", "jwt.token.sign_invalid"),
	JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "OSAEK-10116", "jwt.token.not_found"),

	// 패스워드 관련
	PASSWORD_NOT_FOUND(HttpStatus.UNAUTHORIZED, "OSAEK-10215", "password.not_found"),
	PASSWORD_NOT_CORRECT(HttpStatus.UNAUTHORIZED, "OSAEK-10216", "password.not_correct"),
	OAUTH_PASSWORD_CANT_CHANGE(HttpStatus.NOT_ACCEPTABLE, "OSAEK-10217", "password.cant_change"),

	// 지역 코드 관련
	AREA_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-10009", "area.code.not_found"),
  
	// S3 코드 관련
	S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10510", "s3.upload.fail"),
	S3_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10511", "s3.delete.fail"),

	// 스토리 관련
	STORY_CODE_INVALID(HttpStatus.BAD_REQUEST, "OSAEK-30001", "story.code.invalid"),
	STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "OSAEK-30002", "story.not_found"),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "OSAEK-10511", "unsupported.media_type"),
	THUMBNAIL_CANT_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10512", "thumbnail.cant_create"),
	TEMPFILE_CANT_CREATE(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10513", "tempfile.cant_create"),

	// 미디어 업로드 관련
	FILE_EMPTY(HttpStatus.BAD_REQUEST, "OSAEK-10514", "file.empty"),
	FILE_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "OSAEK-10515", "file.size_exceed"),
	MEDIA_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10516", "media.upload.fail"),
	THUMBNAIL_GENERATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "OSAEK-10517", "thumbnail.generation.fail"),

	// 축제 관련
	FESTA_ID_NULL(HttpStatus.BAD_REQUEST, "OSAEK-30002", "festa.id.null"),
	FESTA_ID_INVALID(HttpStatus.BAD_REQUEST, "OSAEK-30003", "festa.id.invalid");

	private final HttpStatus httpStatus;
	private final String code;
	private final String messageKey;
}
