package com.oseak.myFestaBackend.entity;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubArea {
	@EmbeddedId
	private SubAreaId id;

	@Column(name = "sub_region_name", nullable = false)
	@Comment("하위지역명")
	private String subAreaName;
}
