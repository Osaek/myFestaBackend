package com.oseak.myFestaBackend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * <p>모든 API 응답에 공통적으로 사용되는 래퍼 클래스입니다.</p>
 *
 * <p>성공 응답은 {@code success()}을 통해 생성되며,
 * 실패 응답은 {@code fail()}을 통해 생성합니다.</p>
 *
 * @param <T> 실제 응답 데이터의 타입
 */
@Schema(description = "공통 응답 래퍼")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

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
	 * @param code    커스텀 에러 코드 (성공 시 null)
	 * @param message 메시지 (성공 시 null)
	 * @param data    응답 데이터
	 */
	private CommonResponse(String code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 * 성공 응답을 생성합니다.
	 *
	 * @param data 응답 데이터
	 * @return CommonResponse 인스턴스
	 */
	public static <T> CommonResponse<T> success(T data) {
		return new CommonResponse<>(null, null, data);
	}

	/**
	 * 본문 없이 성공 응답 (No Content)을 생성합니다.
	 *
	 * @return CommonResponse 인스턴스
	 */
	public static <T> CommonResponse<T> noContent() {
		return new CommonResponse<>(null, null, null);
	}

	/**
	 * 실패 응답을 생성합니다.
	 *
	 * @param code    커스텀 에러 코드
	 * @param message 사용자에게 전달할 메시지
	 * @return CommonResponse 인스턴스
	 */
	public static <T> CommonResponse<T> fail(String code, String message) {
		return new CommonResponse<>(code, message, null);
	}

	/**
	 * 실패 응답을 생성합니다.
	 *
	 * @param code    커스텀 에러 코드
	 * @param message 사용자에게 전달할 메시지
	 * @param data    응답 데이터
	 * @return CommonResponse 인스턴스
	 */
	public static <T> CommonResponse<T> fail(String code, String message, T data) {
		return new CommonResponse<>(code, message, data);
	}

}
