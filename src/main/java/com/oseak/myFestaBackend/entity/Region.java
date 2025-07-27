package com.oseak.myFestaBackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor
public class Region {
	@Id
	@Column(name = "region_code")
	private Integer regionCode;

	@Column(name = "region_name", nullable = false)
	private String regionName;
}
