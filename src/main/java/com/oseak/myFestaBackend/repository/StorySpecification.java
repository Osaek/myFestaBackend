package com.oseak.myFestaBackend.repository;

import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.oseak.myFestaBackend.dto.request.StorySearchRequestDto;
import com.oseak.myFestaBackend.entity.Story;

public class StorySpecification {
	public static Specification<Story> createSpecification(StorySearchRequestDto request, Long viewerMemberId) {
		return Specification.allOf(
			notDeleted(),
			memberIdEquals(request.getMemberId()),
			festaIdEquals(request.getFestaId()),
			keywordContains(request.getKeyword()),
			visibilityRule(request.getMemberId(), viewerMemberId)
		);
	}

	private static Specification<Story> notDeleted() {
		return (root, q, cb) -> cb.isFalse(root.get("isDeleted"));
	}

	private static Specification<Story> memberIdEquals(Long memberId) {
		if (memberId == null)
			return null;
		return (root, q, cb) -> cb.equal(root.get("memberId"), memberId);
	}

	private static Specification<Story> festaIdEquals(Long festaId) {
		if (festaId == null)
			return null;
		return (root, q, cb) -> cb.equal(root.get("festaId"), festaId);
	}

	/**
	 * keyword in (festaName) - case-insensitive, wildcard escape
	 */
	private static Specification<Story> keywordContains(String keyword) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(keyword))
				return cb.conjunction();

			String escaped = escapeLike(keyword.trim().toLowerCase());
			String pattern = "%" + escaped + "%";

			return cb.or(
				cb.like(cb.lower(root.get("festaName")), pattern, '\\')
			);
		};
	}

	/**
	 * 가시성 규칙:
	 * - targetMemberId == viewerMemberId (내 스토리 조회) 이면 isOpen 필터 미적용
	 * - 그 외(남의/전체/축제상세) 공개글만
	 */
	private static Specification<Story> visibilityRule(Long targetMemberId, Long viewerMemberId) {
		boolean isMine = (targetMemberId != null) && Objects.equals(targetMemberId, viewerMemberId);
		return isMine ? null : (root, q, cb) -> cb.isTrue(root.get("isOpen"));
	}

	/**
	 * LIKE 와일드카드(% _ \) 이스케이프
	 */
	private static String escapeLike(String input) {
		return input.replace("\\", "\\\\")
			.replace("%", "\\%")
			.replace("_", "\\_");
	}

}
