package com.oseak.myFestaBackend.entity;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "review")
public class Review {

	@EmbeddedId
	private ReviewId id;

	@MapsId("festaId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "festa_id", referencedColumnName = "festa_id", nullable = false)
	private Festa festa;

	@Column(name = "score")
	private Double score;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "description", length = 1000)
	private String description;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDate createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDate updatedAt;

	@Builder
	public Review(Long memberId, Festa festa, Double score, String imageUrl, String description) {
		this.id = new ReviewId(memberId, festa.getFestaId());
		this.festa = festa;
		this.score = score;
		this.imageUrl = imageUrl;
		this.description = description;
	}

	public void update(Double score, String imageUrl, String description) {
		if (score != null)
			this.score = score;
		if (imageUrl != null)
			this.imageUrl = imageUrl;
		if (description != null)
			this.description = description;
	}
}
