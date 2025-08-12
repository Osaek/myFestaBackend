package com.oseak.myFestaBackend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.oseak.myFestaBackend.entity.enums.FestaStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "festa")
public class Festa {

	@Id
	@Column(name = "festa_id")
	private Long festaId;

	@Column(name = "festa_name", nullable = false)
	private String festaName;

	private Double latitude;

	private Double longitude;

	@Column(name = "festa_address")
	private String festaAddress;

	@Column(name = "festa_start_at")
	private LocalDate festaStartAt;

	@Column(name = "festa_end_at")
	private LocalDate festaEndAt;

	@Column(name = "area_code")
	private Integer areaCode;

	@Column(name = "sub_area_code")
	private Integer subAreaCode;

	@Column(columnDefinition = "TEXT")
	private String overview;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "open_time")
	private String openTime;

	@Column(name = "fee_info")
	private String feeInfo;

	@Enumerated(EnumType.STRING)
	@Column(name = "festa_status", nullable = false)
	private FestaStatus festaStatus;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	public Festa(Long festaId, String festaName, Double latitude, Double longitude, String festaAddress,
		LocalDate festaStartAt, LocalDate festaEndAt, Integer areaCode, Integer subAreaCode,
		String overview, String description, String imageUrl, String openTime, String feeInfo,
		FestaStatus festaStatus) {
		this.festaId = festaId;
		this.festaName = festaName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.festaAddress = festaAddress;
		this.festaStartAt = festaStartAt;
		this.festaEndAt = festaEndAt;
		this.areaCode = areaCode;
		this.subAreaCode = subAreaCode;
		this.overview = overview;
		this.description = description;
		this.imageUrl = imageUrl;
		this.openTime = openTime;
		this.feeInfo = feeInfo;
		this.festaStatus = festaStatus;
	}

	public void updateContent(String overview, String description) {
		this.overview = overview;
		this.description = description;
	}

	public void updateIntro(String playtime, String feeInfo) {
		if (playtime != null && !playtime.isBlank()) {
			this.openTime = playtime;
		}
		if (feeInfo != null && !feeInfo.isBlank()) {
			this.feeInfo = feeInfo;
		}
	}

	public void updateStatus(FestaStatus status) {
		this.festaStatus = status;
	}
}
