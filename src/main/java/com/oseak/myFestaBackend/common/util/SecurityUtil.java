package com.oseak.myFestaBackend.common.util;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;

@Component
public class SecurityUtil {

	public static CustomUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			return (CustomUserDetails)authentication.getPrincipal();
		}

		throw new OsaekException(AUTH_CREDENTIALS_INVALID);
	}

	public static Long getCurrentUserId() {
		return getCurrentUser().getMemberId();
	}

	public static Member getCurrentMember() {
		return getCurrentUser().getMember();
	}
}
