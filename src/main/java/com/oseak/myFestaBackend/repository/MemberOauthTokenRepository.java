package com.oseak.myFestaBackend.repository;

import com.oseak.myFestaBackend.domain.MemberOauthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberOauthTokenRepository extends JpaRepository<MemberOauthToken, Long> {
}
