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
			visibilityRule(request, viewerMemberId)
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
	 * 공개/비공개 규칙
	 * - 기본: 공개글만
	 * - 내 프로필 화면(= targetMemberId == viewer) && includePrivateMine=true: 비공개 포함
	 */
	private static Specification<Story> visibilityRule(StorySearchRequestDto req, Long viewerMemberId) {
		boolean mine = (req.getMemberId() != null) && Objects.equals(req.getMemberId(), viewerMemberId);
		boolean allowPrivateMine = mine && req.isIncludePrivateMine();

		// allowPrivateMine일 때만 공개 필터 미적용(= 전체), 그 외에는 공개만
		return allowPrivateMine ? null : (root, q, cb) -> cb.isTrue(root.get("isOpen"));
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
