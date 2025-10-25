package com.oseak.myFestaBackend.config.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger requestLogger = LoggerFactory.getLogger("com.oseak.myFestaBackend.REQUEST");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();

		try {
			filterChain.doFilter(request, response);
		} finally {
			long duration = System.currentTimeMillis() - startTime;

			// Swagger, actuator 등 제외
			String uri = request.getRequestURI();
			if (!uri.startsWith("/swagger") && !uri.startsWith("/v3/api-docs") &&
				!uri.startsWith("/actuator") && !uri.equals("/favicon.ico") && !uri.equals("/error")) {

				requestLogger.info("{} {} - {} - {}ms",
					request.getMethod(),
					uri,
					response.getStatus(),
					duration);
			}
		}
	}
}
