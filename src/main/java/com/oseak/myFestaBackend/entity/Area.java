package com.oseak.myFestaBackend.entity;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Area {
	@Id
	@Column(name = "region_code")
	@Comment("상위지역코드")
	private Integer areaCode;

	@Column(name = "region_name", nullable = false)
	@Comment("상위지역명")
	private String areaName;
}
