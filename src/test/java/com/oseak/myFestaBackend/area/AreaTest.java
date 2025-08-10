package com.oseak.myFestaBackend.area;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.dto.response.AllSubAreaResponseDto;
import com.oseak.myFestaBackend.dto.response.AreaResponseDto;
import com.oseak.myFestaBackend.dto.response.SubAreaResponseDto;
import com.oseak.myFestaBackend.entity.Area;
import com.oseak.myFestaBackend.entity.SubArea;
import com.oseak.myFestaBackend.entity.SubAreaId;
import com.oseak.myFestaBackend.repository.AreaRepository;
import com.oseak.myFestaBackend.repository.SubAreaRepository;
import com.oseak.myFestaBackend.service.AreaService;

@ExtendWith(MockitoExtension.class)
public class AreaTest {
	@Mock
	private AreaRepository areaRepository;

	@Mock
	private SubAreaRepository subAreaRepository;

	@InjectMocks
	private AreaService areaService;

	// Sample data for testing
	private Area seoulArea;
	private Area busanArea;
	private SubArea gangnamSubArea;
	private SubArea songpaSubArea;
	private SubAreaId gangnamId;

	@BeforeEach
	void setUp() {
		seoulArea = new Area(1, "Seoul");
		busanArea = new Area(2, "Busan");

		gangnamId = new SubAreaId(seoulArea.getAreaCode(), 101);
		gangnamSubArea = new SubArea(gangnamId, "Gangnam-gu");

		SubAreaId songpaId = new SubAreaId(seoulArea.getAreaCode(), 102);
		songpaSubArea = new SubArea(songpaId, "Songpa-gu");
	}

	@Test
	@DisplayName("모든 상위 지역 코드 조회 성공")
	void getAllRegions_Success() {
		// Given
		List<Area> areas = List.of(seoulArea, busanArea);
		when(areaRepository.findAll()).thenReturn(areas);

		// When
		List<AreaResponseDto> result = areaService.getAllAreas();

		// Then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getAreaName()).isEqualTo("Seoul");
		assertThat(result.get(1).getAreaName()).isEqualTo("Busan");
		verify(areaRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("상위 지역이 없을 때 빈 리스트 반환")
	void getAllAreas_NoAreasFound() {
		// Given
		when(areaRepository.findAll()).thenReturn(Collections.emptyList());

		// When
		List<AreaResponseDto> result = areaService.getAllAreas();

		// Then
		assertThat(result).isEmpty();
		verify(areaRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("모든 하위 지역 코드 조회 성공")
	void getSubAreas_Success() {
		// Given
		SubAreaId busanSubAreaId = new SubAreaId(busanArea.getAreaCode(), 201);
		SubArea busanSubArea = new SubArea(busanSubAreaId, "Haeundae-gu");
		List<SubArea> subAreas = List.of(gangnamSubArea, songpaSubArea, busanSubArea);
		when(subAreaRepository.findAll()).thenReturn(subAreas);

		// When
		List<AllSubAreaResponseDto> result = areaService.getSubAreas();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getSubAreaName()).isEqualTo("Gangnam-gu");
		assertThat(result.get(2).getSubAreaName()).isEqualTo("Haeundae-gu");
		verify(subAreaRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("하위 지역이 없을 때 빈 리스트 반환")
	void getSubAreas_NoSubAreasFound() {
		// Given
		when(subAreaRepository.findAll()).thenReturn(Collections.emptyList());

		// When
		List<AllSubAreaResponseDto> result = areaService.getSubAreas();

		// Then
		assertThat(result).isEmpty();
		verify(subAreaRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("존재하지 않는 상위 지역 코드일 경우 예외 발생")
	void getSubAreasByAreaCode_NotFound() {
		// Given
		Integer nonExistentAreaCode = 999;
		when(areaRepository.existsById(nonExistentAreaCode)).thenReturn(false);

		// When & Then
		assertThrows(OsaekException.class, () -> {
			areaService.getSubAreasByAreaCode(nonExistentAreaCode);
		});

		verify(areaRepository, times(1)).existsById(nonExistentAreaCode);
	}

	@Test
	@DisplayName("상위 지역은 존재하나, 속한 하위 지역이 없을 때 빈 리스트 반환")
	void getSubAreasByAreaCode_EmptySubAreas() {
		// Given
		Integer busanAreaCode = busanArea.getAreaCode();
		when(areaRepository.existsById(busanAreaCode)).thenReturn(true);

		// When
		List<SubAreaResponseDto> result = areaService.getSubAreasByAreaCode(busanAreaCode);

		// Then
		assertThat(result).isEmpty();
		verify(areaRepository, times(1)).existsById(busanAreaCode);
	}
}
