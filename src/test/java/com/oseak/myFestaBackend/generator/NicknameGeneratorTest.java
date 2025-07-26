package com.oseak.myFestaBackend.generator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.oseak.myFestaBackend.entity.GeneratedNickname;
import com.oseak.myFestaBackend.repository.GeneratedNicknameRepository;
import com.oseak.myFestaBackend.repository.NicknameAdjectiveRepository;
import com.oseak.myFestaBackend.repository.NicknameAnimalRepository;
import com.oseak.myFestaBackend.repository.NicknameVibeRepository;

public class NicknameGeneratorTest {

	private NicknameGenerator generator;
	private NicknameVibeRepository vibeRepo;
	private NicknameAdjectiveRepository adjRepo;
	private NicknameAnimalRepository animalRepo;
	private GeneratedNicknameRepository genRepo;

	@BeforeEach
	void setUp() {
		vibeRepo = mock(NicknameVibeRepository.class);
		adjRepo = mock(NicknameAdjectiveRepository.class);
		animalRepo = mock(NicknameAnimalRepository.class);
		genRepo = mock(GeneratedNicknameRepository.class);

		generator = new NicknameGenerator(vibeRepo, adjRepo, animalRepo, genRepo);
	}

	@Test
	@DisplayName("닉네임이 정상적으로 생성되어 저장된다")
	void generateNickname_success() {
		// given
		when(vibeRepo.findRandomByLangCode("ko")).thenReturn("여름빛");
		when(adjRepo.findRandomByLangCode("ko")).thenReturn("웃는");
		when(animalRepo.findRandomByLangCode("ko")).thenReturn("고양이");

		when(genRepo.save(any())).thenReturn(
			new GeneratedNickname(1L, "여름빛웃는 고양이", LocalDateTime.now())
		);

		// when
		String nickname = generator.generate("ko");

		// then
		assertThat(nickname).isEqualTo("여름빛웃는 고양이");
	}

	@Test
	@DisplayName("기본 닉네임이 30회 모두 충돌 시, 랜덤 숫자 접미사를 붙여 저장된다")
	void generateNickname_withSuffixFallback_success() {
		// given
		when(vibeRepo.findRandomByLangCode("ko")).thenReturn("불꽃");
		when(adjRepo.findRandomByLangCode("ko")).thenReturn("설레는");
		when(animalRepo.findRandomByLangCode("ko")).thenReturn("여우");

		// 기본 닉네임 30회 충돌
		when(genRepo.save(any()))
			.thenThrow(new DataIntegrityViolationException("중복 오류")) // 1~30회 충돌
			.thenThrow(new DataIntegrityViolationException("중복 오류")) // fallback 1 충돌
			.thenReturn(new GeneratedNickname(31L, "불꽃설레는 여우42", LocalDateTime.now())); // fallback 2 성공

		// when
		String nickname = generator.generate("ko");

		// then
		assertThat(nickname).startsWith("불꽃설레는 여우");
		assertThat(nickname).hasSizeLessThanOrEqualTo(32);
	}
}
