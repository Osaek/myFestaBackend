package com.oseak.myFestaBackend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.oseak.myFestaBackend.dto.search.FestaSearchRequestDto;
import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.entity.enums.FestaStatus;

public class FestaSpecification {

	public static Specification<Festa> createSpecification(FestaSearchRequestDto request) {
		return Specification.allOf(
			areaCodeEquals(request.getAreaCode()),
			subAreaCodeEquals(request.getSubAreaCode()),
			keywordContains(request.getKeyword()),
			dateRangeOverlaps(request.getStartDate(), request.getEndDate()),
			statusIn(request.getFestaStatuses())
		);

	}

	/**
	 * areaCode = ?
	 */
	private static Specification<Festa> areaCodeEquals(Integer areaCode) {
		return (root, query, cb) -> {
			if (areaCode == null)
				return cb.conjunction();
			return cb.equal(root.get("areaCode"), areaCode);
		};
	}

	/**
	 * subAreaCode = ?
	 */
	private static Specification<Festa> subAreaCodeEquals(Integer subAreaCode) {
		return (root, query, cb) -> {
			if (subAreaCode == null)
				return cb.conjunction();
			return cb.equal(root.get("subAreaCode"), subAreaCode);
		};
	}

	/**
	 * keyword in (festaName, overview, description) - case-insensitive, wildcard escape
	 */
	private static Specification<Festa> keywordContains(String keyword) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(keyword))
				return cb.conjunction();

			String escaped = escapeLike(keyword.trim().toLowerCase());
			String pattern = "%" + escaped + "%";

			return cb.or(
				cb.like(cb.lower(root.get("festaName")), pattern, '\\'),
				cb.like(cb.lower(root.get("overview")), pattern, '\\'),
				cb.like(cb.lower(root.get("description")), pattern, '\\')
			);
		};
	}

	/**
	 * 날짜 겹침: [festaStartAt, festaEndAt] ∩ [startOfDay(startDate), endOfDay(endDate)] ≠ ∅
	 * 즉, festaStartAt <= 검색종료 && festaEndAt >= 검색시작
	 */
	private static Specification<Festa> dateRangeOverlaps(LocalDate startDate, LocalDate endDate) {
		return (root, query, cb) -> {
			if (startDate == null && endDate == null)
				return cb.conjunction();

			if (startDate != null && endDate != null) {
				return cb.and(
					cb.lessThanOrEqualTo(root.get("festaStartAt"), endDate),
					cb.greaterThanOrEqualTo(root.get("festaEndAt"), startDate)
				);
			} else if (startDate != null) {
				return cb.greaterThanOrEqualTo(root.get("festaEndAt"), startDate);
			} else { // endDate != null
				return cb.lessThanOrEqualTo(root.get("festaStartAt"), endDate);
			}
		};
	}

	/**
	 * 축제 진행 상태 리스트에 포함된 것만 검색
	 */
	private static Specification<Festa> statusIn(List<FestaStatus> statuses) {
		return (root, query, cb) -> {
			if (statuses == null || statuses.isEmpty())
				return cb.conjunction();
			return root.get("festaStatus").in(statuses);
		};
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
