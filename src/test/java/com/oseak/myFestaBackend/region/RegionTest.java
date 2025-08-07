package com.oseak.myFestaBackend.region;

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
import com.oseak.myFestaBackend.dto.response.RegionResponseDto;
import com.oseak.myFestaBackend.dto.response.SubRegionResponseDto;
import com.oseak.myFestaBackend.entity.Region;
import com.oseak.myFestaBackend.entity.SubRegion;
import com.oseak.myFestaBackend.entity.SubRegionId;
import com.oseak.myFestaBackend.repository.RegionRepository;
import com.oseak.myFestaBackend.repository.SubRegionRepository;
import com.oseak.myFestaBackend.service.RegionService;

@ExtendWith(MockitoExtension.class)
public class RegionTest {
	@Mock
	private RegionRepository regionRepository;

	@Mock
	private SubRegionRepository subRegionRepository;

	@InjectMocks
	private RegionService regionService;

	// Sample data for testing
	private Region seoulRegion;
	private Region busanRegion;
	private SubRegion gangnamSubRegion;
	private SubRegion songpaSubRegion;
	private SubRegionId gangnamId;

	@BeforeEach
	void setUp() {
		seoulRegion = new Region(1, "Seoul");
		busanRegion = new Region(2, "Busan");

		gangnamId = new SubRegionId(seoulRegion.getRegionCode(), 101);
		gangnamSubRegion = new SubRegion(gangnamId, "Gangnam-gu");

		SubRegionId songpaId = new SubRegionId(seoulRegion.getRegionCode(), 102);
		songpaSubRegion = new SubRegion(songpaId, "Songpa-gu");
	}

	@Test
	@DisplayName("모든 상위 지역 코드 조회 성공")
	void getAllRegions_Success() {
		// Given
		List<Region> regions = List.of(seoulRegion, busanRegion);
		when(regionRepository.findAll()).thenReturn(regions);

		// When
		List<RegionResponseDto> result = regionService.getAllRegions();

		// Then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getRegionName()).isEqualTo("Seoul");
		assertThat(result.get(1).getRegionName()).isEqualTo("Busan");
		verify(regionRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("상위 지역이 없을 때 빈 리스트 반환")
	void getAllRegions_NoRegionsFound() {
		// Given
		when(regionRepository.findAll()).thenReturn(Collections.emptyList());

		// When
		List<RegionResponseDto> result = regionService.getAllRegions();

		// Then
		assertThat(result).isEmpty();
		verify(regionRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("모든 하위 지역 코드 조회 성공")
	void getSubRegions_Success() {
		// Given
		SubRegionId busanSubRegionId = new SubRegionId(busanRegion.getRegionCode(), 201);
		SubRegion busanSubRegion = new SubRegion(busanSubRegionId, "Haeundae-gu");
		List<SubRegion> subRegions = List.of(gangnamSubRegion, songpaSubRegion, busanSubRegion);
		when(subRegionRepository.findAll()).thenReturn(subRegions);

		// When
		List<SubRegionResponseDto> result = regionService.getSubRegions();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getSubRegionName()).isEqualTo("Gangnam-gu");
		assertThat(result.get(2).getSubRegionName()).isEqualTo("Haeundae-gu");
		verify(subRegionRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("하위 지역이 없을 때 빈 리스트 반환")
	void getSubRegions_NoSubRegionsFound() {
		// Given
		when(subRegionRepository.findAll()).thenReturn(Collections.emptyList());

		// When
		List<SubRegionResponseDto> result = regionService.getSubRegions();

		// Then
		assertThat(result).isEmpty();
		verify(subRegionRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("존재하지 않는 상위 지역 코드일 경우 예외 발생")
	void getSubRegionsByRegionCode_NotFound() {
		// Given
		Integer nonExistentRegionCode = 999;
		when(regionRepository.existsById(nonExistentRegionCode)).thenReturn(false);

		// When & Then
		assertThrows(OsaekException.class, () -> {
			regionService.getSubRegionsByRegionCode(nonExistentRegionCode);
		});

		verify(regionRepository, times(1)).existsById(nonExistentRegionCode);
	}

	@Test
	@DisplayName("상위 지역은 존재하나, 속한 하위 지역이 없을 때 빈 리스트 반환")
	void getSubRegionsByRegionCode_EmptySubRegions() {
		// Given
		Integer busanRegionCode = busanRegion.getRegionCode();
		when(regionRepository.existsById(busanRegionCode)).thenReturn(true);

		// When
		List<SubRegionResponseDto> result = regionService.getSubRegionsByRegionCode(busanRegionCode);

		// Then
		assertThat(result).isEmpty();
		verify(regionRepository, times(1)).existsById(busanRegionCode);
	}
}
