package com.oseak.myFestaBackend.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * 다국어 메시지 처리를 위한 설정 클래스입니다.
 *
 * <p>에러 메시지, 검증 메시지 등의 국제화를 위해 MessageSource Bean을 설정하고,
 * 기본 로케일을 설정하는 LocaleResolver를 등록합니다.</p>
 *
 * <ul>
 *   <li>메시지 파일: classpath:errors_ko.properties, errors_en.properties 등</li>
 *   <li>기본 인코딩: UTF-8</li>
 *   <li>기본 Locale: 한국어</li>
 * </ul>
 */
@Configuration
public class MessageConfig {

	/**
	 * 국제화 메시지를 처리하는 MessageSource Bean을 등록합니다.
	 *
	 * <p>기반 메시지 파일은 `classpath:errors`를 기준으로 하며,
	 * 예: errors.properties, errors_ko.properties, errors_en.properties 등</p>
	 *
	 * @return UTF-8 인코딩 설정된 ReloadableResourceBundleMessageSource
	 */
	@Bean(name = "messageSource")
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasename("classpath:errors"); // 메시지 파일 경로
		source.setDefaultEncoding("UTF-8");     // 인코딩 설정
		return source;
	}

	/**
	 * 기본 로케일을 설정하는 LocaleResolver Bean을 등록합니다.
	 *
	 * <p>Spring MVC에서 사용자의 Locale을 Session 기반으로 관리하며,
	 * 기본 Locale은 한국어(ko_KR)로 설정됩니다.</p>
	 *
	 * @return 기본 로케일이 설정된 SessionLocaleResolver
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver resolver = new SessionLocaleResolver();
		resolver.setDefaultLocale(Locale.KOREA); // 기본 로케일 설정
		return resolver;
	}
}
