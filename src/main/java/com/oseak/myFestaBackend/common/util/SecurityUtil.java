package com.oseak.myFestaBackend.common.util;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityUtil {

	public static CustomUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		log.debug("Getting current user - Authentication: {}", authentication);
		if (authentication != null) {
			log.debug("Authentication principal type: {}", authentication.getPrincipal().getClass().getSimpleName());
			log.debug("Authentication principal: {}", authentication.getPrincipal());
		}

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			log.debug("Successfully extracted CustomUserDetails for memberId: {}", userDetails.getMemberId());
			return userDetails;
		}

		log.error("Failed to get current user - Authentication null or principal not CustomUserDetails");
		throw new OsaekException(AUTH_CREDENTIALS_INVALID);
	}

	public static Long getCurrentUserId() {
		return getCurrentUser().getMemberId();
	}

	public static Long getCurrentUserIdOrNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			return userDetails.getMemberId();
		}

		return null;
	}

	public static Member getCurrentMember() {
		return getCurrentUser().getMember();
	}
}
