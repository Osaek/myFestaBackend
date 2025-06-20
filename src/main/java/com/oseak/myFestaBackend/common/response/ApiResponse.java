package com.oseak.myFestaBackend.common.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * <p>모든 API 응답에 공통적으로 사용되는 래퍼 클래스입니다.</p>
 *
 * <p>성공 응답은 {@code success()}, {@code created()} 등을 통해 생성되며,
 * 실패 응답은 {@code fail()}을 통해 생성합니다.</p>
 *
 * @param <T> 실제 응답 데이터의 타입
 */
@Schema(description = "공통 응답 래퍼")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	/**
	 * HTTP 상태 코드 (예: 200, 201, 400 등)
	 */
	@Schema(description = "HTTP 상태 코드", example = "200")
	private final int status;

	/**
	 * 커스텀 에러 코드. 성공 시 null.
	 * 예: "OSAEK-COM-001"
	 */
	@Schema(description = "에러 코드 (성공 시 null)", example = "OSAEK-COM-001")
	private final String code;

	/**
	 * 사용자에게 보여줄 메시지. 주로 에러 발생 시 사용됨.
	 */
	@Schema(description = "메시지", example = "축제를 찾을 수 없습니다.")
	private final String message;

	/**
	 * 응답 본문 데이터. 성공 시 실제 결과 객체가 담김.
	 */
	@Schema(description = "응답 데이터")
	private final T data;

	/**
	 * 전체 생성자. 응답의 모든 필드를 지정하여 생성합니다.
	 *
	 * @param status  HTTP 상태 코드
	 * @param code    커스텀 에러 코드 (성공 시 null)
	 * @param message 메시지 (성공 시 null)
	 * @param data    응답 데이터 (실패 시 null)
	 */
	private ApiResponse(int status, String code, String message, T data) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 * 성공 응답 (200 OK)을 생성합니다.
	 *
	 * @param data 응답 데이터
	 * @return ApiResponse 인스턴스
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(HttpStatus.OK.value(), null, null, data);
	}

	/**
	 * 리소스 생성 성공 응답 (201 Created)을 생성합니다.
	 *
	 * @param data 생성된 리소스 데이터
	 * @return ApiResponse 인스턴스
	 */
	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(HttpStatus.CREATED.value(), null, null, data);
	}

	/**
	 * 본문 없이 성공 응답 (204 No Content)을 생성합니다.
	 *
	 * @return ApiResponse 인스턴스
	 */
	public static <T> ApiResponse<T> noContent() {
		return new ApiResponse<>(HttpStatus.NO_CONTENT.value(), null, null, null);
	}

	/**
	 * 실패 응답을 생성합니다.
	 *
	 * @param status  HTTP 상태 코드 (예: 400, 404, 500)
	 * @param code    커스텀 에러 코드
	 * @param message 사용자에게 전달할 메시지
	 * @return ApiResponse 인스턴스
	 */
	public static <T> ApiResponse<T> fail(HttpStatus status, String code, String message) {
		return new ApiResponse<>(status.value(), code, message, null);
	}

}
