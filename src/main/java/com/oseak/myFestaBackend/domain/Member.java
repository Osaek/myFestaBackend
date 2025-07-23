package com.oseak.myFestaBackend.domain;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.oseak.myFestaBackend.common.exception.OsaekException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	private String profile;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "is_withdrawn", nullable = false)
	@ColumnDefault("false")
	private boolean isWithdrawn;

	@Column(name = "withdrawn_at")
	private LocalDateTime withdrawnAt;

	@Builder
	public Member(Long memberId, String email, String nickname, Provider provider, String profile) {
		this.id = memberId;
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.profile = profile;
	}

	public enum Provider {
		local, kakao
	}

	public void withdraw() {
		if (this.isWithdrawn) {
			// TODO: 이미 유저 탈퇴된 상태라는 오류 내야함.
			throw new OsaekException(USER_ID_NOT_FOUND);
		}

		isWithdrawn = true;
		withdrawnAt = LocalDateTime.now();
	}
}
