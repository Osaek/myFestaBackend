package com.oseak.myFestaBackend.service;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.dto.member.ChangePasswordRequestDto;
import com.oseak.myFestaBackend.dto.member.ChangePasswordResponseDto;
import com.oseak.myFestaBackend.dto.member.CreateUserRequestDto;
import com.oseak.myFestaBackend.dto.member.CreateUserResponseDto;
import com.oseak.myFestaBackend.dto.member.WithdrawMemberResponseDto;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.MemberPassword;
import com.oseak.myFestaBackend.entity.enums.Provider;
import com.oseak.myFestaBackend.generator.NicknameGenerator;
import com.oseak.myFestaBackend.generator.ProfileGenerator;
import com.oseak.myFestaBackend.repository.MemberPasswordRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberPasswordRepository memberPasswordRepository;
	private final PasswordEncoder passwordEncoder;
	private final NicknameGenerator nicknameGenerator;
	private final ProfileGenerator profileGenerator;

	// 로컬 회원가입 로직
	@Transactional
	public CreateUserResponseDto createUser(CreateUserRequestDto request) {

		if (memberRepository.existsByEmailAndIsWithdrawnIsFalse(request.getEmail())) {
			log.warn("Signup failed - duplicate email: {}", request.getEmail());
			throw new OsaekException(USER_EMAIL_DUPLICATE);
		}

		String nickname = nicknameGenerator.generate("ko");
		String profile = profileGenerator.getRandomProfileImagePath();

		Member member = Member.builder()
			.email(request.getEmail())
			.nickname(nickname)
			.profile(profile)
			.provider(Provider.LOCAL)
			.build();
		Member savedMember = memberRepository.save(member);

		String encodedPassword = passwordEncoder.encode(request.getPassword());

		MemberPassword memberPassword = MemberPassword.builder()
			.memberId(savedMember.getId())
			.password(encodedPassword)
			.build();

		memberPasswordRepository.save(memberPassword);

		log.info("Signup successful - email: {}", request.getEmail());
		return new CreateUserResponseDto(savedMember);
	}

	// 로컬 회원 삭제
	@Transactional
	public WithdrawMemberResponseDto withdrawMember(Long memberId) {
		Member member = memberRepository.findByIdAndIsWithdrawnIsFalse(memberId)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));

		member.withdraw();
		memberRepository.save(member);

		MemberPassword memberPassword = memberPasswordRepository.findByMemberId(memberId)
			.orElseThrow(() -> new OsaekException(PASSWORD_NOT_FOUND));

		memberPasswordRepository.delete(memberPassword);

		log.info("Withdrawal successful - email: {}", member.getEmail());
		return WithdrawMemberResponseDto.builder()
			.email(member.getEmail())
			.nickname(member.getNickname())
			.build();
	}

	// 로컬 회원 수정
	@Transactional
	public ChangePasswordResponseDto changePassword(ChangePasswordRequestDto request, Long memberId) {
		Member member = memberRepository.findByIdAndIsWithdrawnIsFalse(memberId)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));

		MemberPassword memberPassword = memberPasswordRepository.findByMemberId(memberId)
			.orElseThrow(() -> new OsaekException(OAUTH_PASSWORD_CANT_CHANGE));

		if (!passwordEncoder.matches(request.getCurrentPassword(), memberPassword.getPassword())) {
			throw new OsaekException(PASSWORD_NOT_CORRECT);
		}
		String encodedPassword = passwordEncoder.encode(request.getNewPassword());
		memberPassword.changePassword(encodedPassword);
		memberPasswordRepository.save(memberPassword);

		return ChangePasswordResponseDto.builder()
			.message("Password changed successfully!")
			.build();
	}

	public String getNicknameByMemberId(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new OsaekException(USER_ID_NOT_FOUND));
		return member.getNickname();
	}
}
