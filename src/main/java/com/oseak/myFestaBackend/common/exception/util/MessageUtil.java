package com.oseak.myFestaBackend.common.exception.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * 다국어 메시지 추출을 위한 유틸 클래스입니다.
 * message.properties에 정의된 messageKey 기반으로 로컬라이즈된 메시지를 반환합니다.
 */
@Schema(description = "다국어 메시지 추출을 위한 유틸 클래스")
@Component
@RequiredArgsConstructor
public class MessageUtil {

	private final MessageSource messageSource;

	/**
	 * 현재 Locale 기준으로 메시지 키에 해당하는 로컬라이즈 메시지를 반환합니다.
	 *
	 * @param messageKey 메시지 키 (예: user.id.not_found)
	 * @return 로컬라이즈된 메시지 문자열
	 */
	public String getMessage(String messageKey) {
		return messageSource.getMessage(messageKey, new Object[]{}, LocaleContextHolder.getLocale());
	}

	/**
	 * 지정된 Locale 기준으로 메시지 키에 해당하는 로컬라이즈 메시지를 반환합니다.
	 *
	 * @param messageKey 메시지 키 (예: user.id.not_found)
	 * @param locale 사용할 로케일
	 * @return 로컬라이즈된 메시지 문자열
	 */
	public String getMessage(String messageKey, Locale locale) {
		return messageSource.getMessage(messageKey, new Object[]{}, locale);
	}

}
