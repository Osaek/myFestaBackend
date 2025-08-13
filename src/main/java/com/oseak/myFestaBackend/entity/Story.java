package com.oseak.myFestaBackend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "story")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Story {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_id", nullable = false, updatable = false)
	private Long storyId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "is_open", nullable = false)
	private Boolean isOpen;

	@Column(name = "story_s3_url", length = 500, nullable = false)
	private String storyS3Url;

	@Column(name = "festa_id")
	private Long festaId;

	@Column(name = "festa_name", length = 255)
	private String festaName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.isOpen == null) {
			this.isOpen = true;
		}
		this.isDeleted = false;
	}

	/**
	 * 논리 삭제: 공개를 끄고 삭제 플래그 ON
	 */
	public void softDelete() {
		this.isOpen = false;
		this.isDeleted = true;
	}

	/**
	 * 스토리 비공개
	 */
	public void hideStory() {
		this.isOpen = false;
	}

	/**
	 * 스토리 공개
	 */
	public void openStory() {
		this.isOpen = true;
	}

}
