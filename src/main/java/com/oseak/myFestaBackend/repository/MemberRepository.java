package com.oseak.myFestaBackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);

	Optional<Member> findByEmailAndIsWithdrawnIsFalse(String email);

	Optional<Member> findByIdAndIsWithdrawnIsFalse(Long id);

	boolean existsByEmailAndIsWithdrawnIsFalse(String email);
}
