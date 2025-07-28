package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.MemberPassword;
import com.oseak.myFestaBackend.entity.enums.Provider;
import com.oseak.myFestaBackend.repository.MemberPasswordRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

	private final MemberPasswordRepository memberPasswordRepository;
	private final MemberRepository memberRepository;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public UserDetails loadUserByUsername(String email) {

		Member member = memberRepository.findByEmailAndIsWithdrawnIsFalse(email)
			.orElseThrow(() -> new OsaekException(USER_EMAIL_NOT_FOUND));

		if (member.getProvider() == Provider.LOCAL) {
			MemberPassword memberPassword = memberPasswordRepository.findByMemberId(member.getId())
				.orElseThrow(() -> new OsaekException(PASSWORD_NOT_FOUND));

			return new CustomUserDetails(member, memberPassword);
		}

		return new CustomUserDetails(member, null);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public Member loadMemberById(Long id) {

		Member member = memberRepository.findByIdAndIsWithdrawnIsFalse(id)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));

		return member;
	}
}
