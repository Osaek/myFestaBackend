package com.oseak.myFestaBackend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oseak.myFestaBackend.entity.MemberRefreshToken;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {

	Optional<MemberRefreshToken> findByMemberId(Long memberId);

	Optional<MemberRefreshToken> findByToken(String token);

	@Modifying
	@Query("DELETE FROM MemberRefreshToken mrt WHERE mrt.memberId = :memberId")
	void deleteByMemberId(@Param("memberId") Long memberId);

	@Modifying
	@Query("DELETE FROM MemberRefreshToken mrt WHERE mrt.createdAt < :expiredDate")
	int deleteExpiredTokens(@Param("expiredDate") LocalDateTime expiredDate);
}
