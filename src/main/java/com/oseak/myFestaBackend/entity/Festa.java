package com.oseak.myFestaBackend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "festa_id")
	private Long festaId;

	@Column(name = "festa_name", nullable = false)
	private String festaName;

	private Double latitude;

	private Double longitude;

	@Column(name = "festa_address")
	private String festaAddress;

	@Column(name = "festa_start_at")
	private LocalDateTime festaStartAt;

	@Column(name = "festa_end_at")
	private LocalDateTime festaEndAt;

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

	@Column(name = "festa_status")
	private String festaStatus;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	public Festa(String festaName, Double latitude, Double longitude, String festaAddress,
		LocalDateTime festaStartAt, LocalDateTime festaEndAt, Integer areaCode, Integer subAreaCode,
		String overview, String description, String imageUrl, String openTime, String feeInfo,
		String festaStatus) {
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

	public void updateStatus(String status) {
		this.festaStatus = status;
	}
}
