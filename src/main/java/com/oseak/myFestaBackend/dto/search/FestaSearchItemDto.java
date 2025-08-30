package com.oseak.myFestaBackend.dto.search;

import java.time.LocalDate;

import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.entity.enums.FestaStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "축제 검색 결과 항목")
public class FestaSearchItemDto {

	@Schema(description = "축제 ID", example = "3481597")
	private Long festaId;

	@Schema(description = "축제명", example = "페인터즈")
	private String festaName;

	@Schema(description = "위도", example = "37.5681316804")
	private Double latitude;

	@Schema(description = "경도", example = "126.9696495605")
	private Double longitude;

	@Schema(description = "축제 주소", example = "서울특별시 중구 정동길 3 (정동)")
	private String festaAddress;

	@Schema(description = "축제 시작일", example = "2022-11-01")
	private LocalDate festaStartAt;

	@Schema(description = "축제 종료일", example = "2025-12-31")
	private LocalDate festaEndAt;

	@Schema(description = "지역 코드", example = "1")
	private Integer areaCode;

	@Schema(description = "세부 지역 코드", example = "24")
	private Integer subAreaCode;

	@Schema(description = "개요", example = "2008년 초연된 페인터즈는 화려한 안무와 라이브 드로잉...")
	private String overview;

	@Schema(description = "상세 설명", example = "페인터즈는 관객과 소통하는 주요 매개가 춤과 연기...")
	private String description;

	@Schema(description = "이미지 URL", example = "http://tong.visitkorea.or.kr/cms/resource/...")
	private String imageUrl;

	@Schema(description = "운영 시간", example = "10:00-18:00")
	private String openTime;

	@Schema(description = "요금 정보", example = "성인 15,000원, 청소년 12,000원")
	private String feeInfo;

	@Schema(description = "축제 상태", example = "ONGOING")
	private FestaStatus festaStatus;

	public static FestaSearchItemDto from(Festa festa) {
		return FestaSearchItemDto.builder()
			.festaId(festa.getFestaId())
			.festaName(festa.getFestaName())
			.latitude(festa.getLatitude())
			.longitude(festa.getLongitude())
			.festaAddress(festa.getFestaAddress())
			.festaStartAt(festa.getFestaStartAt())
			.festaEndAt(festa.getFestaEndAt())
			.areaCode(festa.getAreaCode())
			.subAreaCode(festa.getSubAreaCode())
			.overview(festa.getOverview())
			.description(festa.getDescription())
			.imageUrl(festa.getImageUrl())
			.openTime(festa.getOpenTime())
			.feeInfo(festa.getFeeInfo())
			.festaStatus(festa.getFestaStatus())
			.build();
	}
}