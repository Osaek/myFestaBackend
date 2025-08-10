package com.oseak.myFestaBackend.generator;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

/**
 * 프로필 생성기
 * 최대 20개의 프로필 중 하나의 경로를 반환함
 */
@Component
public class ProfileGenerator {
	private static final int MAX_PROFILES = 20;

	/**
	 * profile01.svg ~ profile20.svg 총 20개의 svg중 하나의 경로를 선택
	 * @return 프로필 경로
	 */
	public String getRandomProfileImagePath() {
		int index = ThreadLocalRandom.current().nextInt(1, MAX_PROFILES + 1);
		return "/images/profiles/profile" + String.format("%02d", index) + ".svg";
	}
}
