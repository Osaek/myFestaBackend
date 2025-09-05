package com.oseak.myFestaBackend.dto.request;

import java.util.List;

import com.oseak.myFestaBackend.common.request.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Schema(description = "근처 축제 조회 요청")
public class FestaNearRequestDto extends PageRequest {

	@Schema(description = "현재 위치 위도", example = "37.5665", required = true)
	private Double latitude;

	@Schema(description = "현재 위치 경도", example = "126.9780", required = true)
	private Double longitude;

	@Schema(description = "조회 반경(KM). 허용값: 1, 5, 10, 20", example = "10", defaultValue = "10")
	private Integer distanceKm;

	@Schema(hidden = true)
	public int getValidDistanceKm() {
		List<Integer> allowed = List.of(1, 5, 10, 20);
		return (distanceKm != null && allowed.contains(distanceKm)) ? distanceKm : 10;
	}
}
