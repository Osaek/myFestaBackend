package com.oseak.myFestaBackend.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ReviewId implements Serializable {

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "festa_id", nullable = false)
	private Long festaId;
}