package com.oseak.myFestaBackend.entity;

import java.io.Serializable;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
public class SubAreaId implements Serializable {
	@Column(name = "region_code", nullable = false)
	@Comment("상위지역코드")
	private Integer areaCode;

	@Column(name = "sub_region_code", nullable = false)
	@Comment("하위지역코드")
	private Integer subAreaCode;
}
