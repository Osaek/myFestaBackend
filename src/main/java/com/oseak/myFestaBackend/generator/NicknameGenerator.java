package com.oseak.myFestaBackend.generator;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.entity.GeneratedNickname;
import com.oseak.myFestaBackend.repository.GeneratedNicknameRepository;
import com.oseak.myFestaBackend.repository.NicknameAdjectiveRepository;
import com.oseak.myFestaBackend.repository.NicknameAnimalRepository;
import com.oseak.myFestaBackend.repository.NicknameVibeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 닉네임을 랜덤으로 조합하여 생성하는 유틸성 컴포넌트.
 * - vibe + adjective + animal 형태로 조합된 축제 느낌의 닉네임 생성
 * - 예: "불꽃설레는 여우", "여름빛흔들리는 고양이"
 * - 생성된 닉네임은 `generated_nickname` 테이블에 저장 (UNIQUE 제약)
 * - 동일한 닉네임 충돌 시 최대 30회까지 재시도
 * - 30회 모두 실패 시, 기존 닉네임에 2자리 숫자 접미사(랜덤) 붙여 생성
 * - 예: "불꽃설레는 여우72"
 * - 접미사 충돌 시 무한 재시도
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * String nickname = nicknameGenerator.generate("ko");
 *
 * memberRepository.save(Member.builder()
 *     .email(email)
 *     .nickname(nickname)
 *     .password(passwordEncoder.encode(rawPassword))
 *     .build());
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NicknameGenerator {

	private static final int MAX_ATTEMPTS = 30;
	private static final int FALLBACK_MAX_ATTEMPTS = 300;
	private static final int MAX_NICKNAME_LENGTH = 32;

	private final NicknameVibeRepository vibeRepository;
	private final NicknameAdjectiveRepository adjectiveRepository;
	private final NicknameAnimalRepository animalRepository;
	private final GeneratedNicknameRepository generatedNicknameRepository;

	private final Random random = new Random();

	/**
	 * 언어 코드에 따른 고유 닉네임을 생성하여 저장하고 반환한다.
	 * 충돌 발생 시 최대 30회까지 기본 조합으로 재시도하고,
	 * 이후에는 2자리 숫자 접미사를 붙여 중복 방지 닉네임을 만든다.
	 *
	 * @param langCode 언어 코드 (예: "ko", "en")
	 * @return 생성된 고유 닉네임 문자열
	 * @throws OsaekException 고유 닉네임 생성 실패 시
	 */
	@Transactional
	public String generate(String langCode) {
		String baseNickname = null;

		// 기본 조합으로 최대 30회 시도
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			String nickname = buildNickname(langCode);

			try {
				generatedNicknameRepository.save(
					GeneratedNickname.builder()
						.nickname(nickname)
						.build()
				);

				log.debug("[닉네임 생성] {}회 시도 후 성공 → {}", i + 1, nickname);
				return nickname;

			} catch (DataIntegrityViolationException e) {
				log.debug("[닉네임 충돌] 중복된 닉네임 '{}', {}회 시도 중", nickname, i + 1);
			}

			baseNickname = nickname;
		}

		// fallback: 접미사 붙이기 (중복 피할 때까지 최대 300회 시도)
		for (int i = 0; i < FALLBACK_MAX_ATTEMPTS; ++i) {
			String suffix = String.format("%03d", random.nextInt(999) + 1); // 001~999
			String nicknameWithSuffix = baseNickname + suffix;

			// 길이 초과 시: 먼저 공백 제거 후 접미사 보존 시도
			if (nicknameWithSuffix.length() > MAX_NICKNAME_LENGTH) {
				nicknameWithSuffix = baseNickname.replace(" ", "") + suffix;
			}

			// 공백 제거 후에도 길다면 잘라냄 (접미사 포함 우선)
			if (nicknameWithSuffix.length() > MAX_NICKNAME_LENGTH) {
				nicknameWithSuffix = nicknameWithSuffix.substring(0, MAX_NICKNAME_LENGTH);
			}

			try {
				generatedNicknameRepository.save(
					GeneratedNickname.builder()
						.nickname(nicknameWithSuffix)
						.build()
				);

				log.debug("[닉네임 fallback] 접미사 '{}' 붙여 생성 성공 → {}", suffix, nicknameWithSuffix);
				return nicknameWithSuffix;

			} catch (DataIntegrityViolationException e) {
				log.debug("[닉네임 fallback 충돌] '{}'", nicknameWithSuffix);
			}
		}

		throw new OsaekException(USER_NICKNAME_GENERATION_ATTEMPT_EXCEEDED);

	}

	private String buildNickname(String langCode) {
		String vibe = vibeRepository.findRandomByLangCode(langCode);
		String adjective = adjectiveRepository.findRandomByLangCode(langCode);
		String animal = animalRepository.findRandomByLangCode(langCode);

		return vibe + " " + adjective + " " + animal;
	}
}
