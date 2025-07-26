package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oseak.myFestaBackend.entity.NicknameVibe;

/**
 * 닉네임 분위기 키워드(vibe)를 조회하는 Repository.
 * 주어진 언어 코드(langCode)에 해당하는 분위기 키워드를 랜덤으로 1개 선택한다.
 */
public interface NicknameVibeRepository extends JpaRepository<NicknameVibe, Long> {

	/**
	 * 지정된 언어 코드에 해당하는 분위기 키워드 중 랜덤으로 1개를 조회한다.
	 *
	 * @param langCode 사용할 언어 코드 (예: "ko", "en")
	 * @return 랜덤으로 선택된 분위기 키워드 (word)
	 */
	@Query(value = """
		    SELECT word 
		    FROM nickname_vibe 
		    WHERE lang_code = :langCode 
		    ORDER BY RAND() 
		    LIMIT 1
		""", nativeQuery = true)
	String findRandomByLangCode(@Param("langCode") String langCode);
}
