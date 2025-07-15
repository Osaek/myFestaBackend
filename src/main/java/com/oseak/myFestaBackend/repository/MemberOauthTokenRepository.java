package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.MemberOauthToken;

public interface MemberOauthTokenRepository extends JpaRepository<MemberOauthToken, Long> {
}
