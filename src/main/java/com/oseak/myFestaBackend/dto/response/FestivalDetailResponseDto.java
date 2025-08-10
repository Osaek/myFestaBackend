package com.oseak.myFestaBackend.dto.response;

import java.time.LocalDateTime;

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
@Schema(description = "축제 상세 조회 결과")
public class FestivalDetailResponseDto {

	@Schema(description = "축제 ID", example = "30")
	private Long festaId;

	@Schema(description = "컨텐츠 ID", example = "3481597")
	private Long contentId;

	@Schema(description = "축제명", example = "페인터즈")
	private String festaName;

	@Schema(description = "위도", example = "37.5681316804")
	private Double latitude;

	@Schema(description = "경도", example = "126.9696495605")
	private Double longitude;

	@Schema(description = "축제 주소", example = "서울특별시 중구 정동길 3 (정동)")
	private String festaAddress;

	@Schema(description = "축제 시작 일시", example = "2022-11-01T00:00:00")
	private LocalDateTime festaStartAt;

	@Schema(description = "축제 종료 일시", example = "2025-12-31T00:00:00")
	private LocalDateTime festaEndAt;

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

	public static FestivalDetailResponseDto from(Festa festival) {
		return FestivalDetailResponseDto.builder()
			.festaId(festival.getFestaId())
			.contentId(festival.getContentId())
			.festaName(festival.getFestaName())
			.latitude(festival.getLatitude())
			.longitude(festival.getLongitude())
			.festaAddress(festival.getFestaAddress())
			.festaStartAt(festival.getFestaStartAt())
			.festaEndAt(festival.getFestaEndAt())
			.areaCode(festival.getAreaCode())
			.subAreaCode(festival.getSubAreaCode())
			.overview(festival.getOverview())
			.description(festival.getDescription())
			.imageUrl(festival.getImageUrl())
			.openTime(festival.getOpenTime())
			.feeInfo(festival.getFeeInfo())
			.festaStatus(festival.getFestaStatus())
			.build();
	}
}
