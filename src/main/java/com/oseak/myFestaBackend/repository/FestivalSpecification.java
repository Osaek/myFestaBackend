package com.oseak.myFestaBackend.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.oseak.myFestaBackend.dto.request.FestivalSearchRequest;
import com.oseak.myFestaBackend.entity.Festa;

public class FestivalSpecification {

	public static Specification<Festa> createSpecification(FestivalSearchRequest request) {
		return Specification.allOf(
			areaCodeEquals(request.getAreaCode()),
			subAreaCodeEquals(request.getSubAreaCode()),
			keywordContains(request.getKeyword()),
			dateRangeOverlaps(request.getStartDate(), request.getEndDate())
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
				LocalDateTime start = startDate.atStartOfDay();
				LocalDateTime end = endDate.atTime(LocalTime.MAX);
				return cb.and(
					cb.lessThanOrEqualTo(root.get("festaStartAt"), end),
					cb.greaterThanOrEqualTo(root.get("festaEndAt"), start)
				);
			} else if (startDate != null) {
				LocalDateTime start = startDate.atStartOfDay();
				return cb.greaterThanOrEqualTo(root.get("festaEndAt"), start);
			} else { // endDate != null
				LocalDateTime end = endDate.atTime(LocalTime.MAX);
				return cb.lessThanOrEqualTo(root.get("festaStartAt"), end);
			}
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
