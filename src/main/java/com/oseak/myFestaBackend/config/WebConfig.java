package com.oseak.myFestaBackend.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	/**
	 * 정적 리소스 핸들러 등록
	 * 클라이언트가 "/images/profiles/**" 경로로 요청할 경우,
	 * 프로젝트 내 "/static/images/profiles/" 디렉토리에 있는 파일을 응답함
	 * 성능을 고려하여 응답 시 HTTP Cache-Control 헤더를 설정하여 브라우저가 해당 파일을 최대 1년간 캐시하도록 지정
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/profiles/**")
			.addResourceLocations("classpath:/static/images/profiles/")
			.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
	}
}

