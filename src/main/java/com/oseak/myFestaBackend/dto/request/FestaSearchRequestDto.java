package com.oseak.myFestaBackend.dto.request;

import java.time.LocalDate;

import com.oseak.myFestaBackend.common.request.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "축제 검색 요청")
public class FestaSearchRequestDto extends PageRequest {
	@Schema(description = "지역 코드", example = "1")
	private Integer areaCode;

	@Schema(description = "세부 지역 코드", example = "11")
	private Integer subAreaCode;

	@Schema(description = "검색 키워드", example = "벚꽃축제")
	private String keyword;

	@Schema(description = "축제 시작일", example = "2024-03-01")
	private LocalDate startDate;

	@Schema(description = "축제 종료일", example = "2024-05-31")
	private LocalDate endDate;
}

