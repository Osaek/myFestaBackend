package com.oseak.myFestaBackend.domain;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

	private final Member member;
	private final MemberPassword memberPassword;

	public Long getMemberId() {
		return member.getId();
	}

	public String getEmail() {
		return member.getEmail();
	}

	public String getNickname() {
		return member.getNickname();
	}

	public Member.Provider getProvider() {
		return member.getProvider();
	}

	// UserDetails 구현
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return memberPassword != null ? memberPassword.getPassword() : "";
	}

	@Override
	public String getUsername() {
		return member.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return !member.isWithdrawn();
	}

	@Override
	public boolean isAccountNonLocked() {
		return !member.isWithdrawn();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !member.isWithdrawn();
	}

	@Override
	public boolean isEnabled() {
		return !member.isWithdrawn();
	}

	public boolean hasPassword() {
		return memberPassword != null;
	}

}
