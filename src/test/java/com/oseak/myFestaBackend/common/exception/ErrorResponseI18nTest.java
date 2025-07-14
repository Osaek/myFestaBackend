package com.oseak.myFestaBackend.common.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

import com.oseak.myFestaBackend.common.exception.util.MessageUtil;

@SpringBootTest
@ActiveProfiles("test")  // 테스트 프로파일 활성화
class ErrorResponseI18nTest {

	@Autowired
	private MessageUtil messageUtil;

	@Test
	@DisplayName("로케일에 따라 에러 메시지가 다르게 반환되는지 확인")
	void testI18nMessages() {
		// Given
		String key = "user.id.not_found";

		Locale korean = new Locale("ko", "KR");
		Locale english = Locale.ENGLISH;

		// When
		LocaleContextHolder.setLocale(korean);
		String messageKo = messageUtil.getMessage(key);

		LocaleContextHolder.setLocale(english);
		String messageEn = messageUtil.getMessage(key);

		// Then
		assertThat(messageKo).isEqualTo("해당 사용자를 찾을 수 없습니다.");
		assertThat(messageEn).isEqualTo("The specified user was not found.");
	}
}
