package com.oseak.myFestaBackend.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 상속 가능한 페이지 요청 부모 클래스
 * SuperBuilder를 통해 상속시 빌터 패턴을 지원
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "페이지 요청 정보")
public class PageRequest {
	public static Integer MAX_SIZE = 100;
	public static Integer DEFAULT_SIZE = 20;

	@Builder.Default
	@Schema(description = "페이지 번호(0부터 시작)", example = "0", defaultValue = "0")
	private Integer page = 0;

	@Builder.Default
	@Schema(description = "페이지 크기", example = "20", defaultValue = "20")
	private Integer size = DEFAULT_SIZE;

	/**
	 * 현재 페이지를 반환하는 메소드
	 *
	 * @return page
	 */
	@Schema(hidden = true)
	public Integer getValidPage() {
		return page != null && page >= 0 ? page : 0;
	}

	/**
	 * 현재 페이지의 size를 반환하는 메소드
	 * size가 존재하고, 0보다 크고, 100보다 작거나 같은 숫자인 경우 size, 아닌 경우 기본값 반환
	 *
	 * @return size 크기
	 */
	@Schema(hidden = true)
	public Integer getValidSize() {
		return size != null && size > 0 && size <= MAX_SIZE ? size : DEFAULT_SIZE;
	}
}
