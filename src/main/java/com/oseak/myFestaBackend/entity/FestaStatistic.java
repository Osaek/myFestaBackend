package com.oseak.myFestaBackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "festa_statistic")
public class FestaStatistic {

	@Id
	@Column(name = "festa_id")
	private Long festaId;

	@Column(name = "total_score", nullable = false)
	private Double totalScore = 0.0;

	@Column(name = "review_count", nullable = false)
	private Long reviewCount = 0L;

	@Column(name = "view_count", nullable = false)
	private Long viewCount = 0L;

	@Column(name = "like_count", nullable = false)
	private Long likeCount = 0L;

	@Column(name = "story_count", nullable = false)
	private Long storyCount = 0L;

	@Builder
	public FestaStatistic(Long festaId, Double totalScore, Long reviewCount, Long viewCount, Long likeCount,
		Long storyCount) {
		this.festaId = festaId;
		if (totalScore != null) {
			this.totalScore = totalScore;
		}
		if (reviewCount != null) {
			this.reviewCount = reviewCount;
		}
		if (viewCount != null) {
			this.viewCount = viewCount;
		}
		if (likeCount != null) {
			this.likeCount = likeCount;
		}
		if (storyCount != null) {
			this.storyCount = storyCount;
		}
	}

	public void addReview(double score) {
		this.totalScore += score;
		this.reviewCount += 1;
	}

	public void updateReview(double newScore, double oldScore) {
		double delta = newScore - oldScore;
		if (Math.abs(delta) < 1e-9) {
			return;
		}
		this.totalScore = Math.max(0.0, this.totalScore + delta);
	}

	public void removeReview(double score) {
		if (this.reviewCount <= 0) {
			return;
		}
		this.reviewCount -= 1;
		if (this.reviewCount == 0) {
			this.totalScore = 0.0;
		} else {
			this.totalScore = Math.max(0.0, this.totalScore - score);
		}
	}
}
