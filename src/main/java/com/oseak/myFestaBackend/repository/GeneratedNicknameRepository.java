package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.GeneratedNickname;

/**
 * 생성된 닉네임(nickname)을 저장 및 중복 체크하는 Repository.
 * 닉네임 중복 여부를 검사하고, 중복되지 않은 경우 저장한다.
 */
public interface GeneratedNicknameRepository extends JpaRepository<GeneratedNickname, Long> {

	/**
	 * 주어진 닉네임이 이미 사용되었는지 여부를 확인한다.
	 *
	 * @param nickname 생성된 닉네임 문자열
	 * @return true: 이미 존재함, false: 사용 가능
	 */
	boolean existsByNickname(String nickname);
}
