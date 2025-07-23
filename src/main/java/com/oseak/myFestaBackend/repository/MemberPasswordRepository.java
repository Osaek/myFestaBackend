package com.oseak.myFestaBackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.domain.MemberPassword;

public interface MemberPasswordRepository extends JpaRepository<MemberPassword, Long> {

	Optional<MemberPassword> findByMemberId(Long memberId);
}
