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
@Table(name = "dev_pick_festa")
public class DevPickFesta {

	@Id
	@Column(name = "content_id")
	private Long contentId;

	@Column(name = "festa_name", nullable = false)
	private String festaName;

	@Column(name = "tagline", length = 255)
	private String tagline;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Builder
	public DevPickFesta(Long contentId, String festaName, String tagline, String imageUrl) {
		this.contentId = contentId;
		this.festaName = festaName;
		this.tagline = tagline;
		this.imageUrl = imageUrl;
	}
}
