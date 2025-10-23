package com.oseak.myFestaBackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.common.exception.code.ClientErrorCode;
import com.oseak.myFestaBackend.common.exception.code.ServerErrorCode;
import com.oseak.myFestaBackend.dto.ReviewResponseDto;
import com.oseak.myFestaBackend.dto.response.ReviewListResponseDto;
import com.oseak.myFestaBackend.entity.Festa;
import com.oseak.myFestaBackend.entity.FestaStatistic;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.Review;
import com.oseak.myFestaBackend.entity.ReviewId;
import com.oseak.myFestaBackend.repository.FestaRepository;
import com.oseak.myFestaBackend.repository.FestaStatisticRepository;
import com.oseak.myFestaBackend.repository.MemberRepository;
import com.oseak.myFestaBackend.repository.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final FestaRepository festaRepository;
	private final FestaStatisticRepository festaStatisticRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createReview(Long memberId, Long festaId, double score, String imageUrl, String description) {
		ReviewId id = new ReviewId(memberId, festaId);
		if (reviewRepository.existsById(id)) {
			throw new OsaekException(ServerErrorCode.DUPLICATE_REQUEST);
		}

		Festa festa = festaRepository.findById(festaId)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.FESTA_NOT_FOUND));

		// Member 존재 여부만 확인
		if (!memberRepository.existsById(memberId)) {
			throw new OsaekException(ClientErrorCode.USER_ID_NOT_FOUND);
		}

		Review review = Review.builder()
			.memberId(memberId)
			.festa(festa)
			.score(score)
			.imageUrl(imageUrl)
			.description(description)
			.build();
		reviewRepository.save(review);

		FestaStatistic stat = festaStatisticRepository.findById(festaId)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.FESTA_NOT_FOUND));
		stat.addReview(score);
		festaStatisticRepository.save(stat);
	}

	@Transactional
	public void updateReview(Long memberId, Long festaId, Double newScore, String imageUrl, String description) {
		ReviewId id = new ReviewId(memberId, festaId);
		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.REVIEW_NOT_FOUND));

		double oldScore = review.getScore() == null ? 0.0 : review.getScore();
		double nextScore = (newScore == null) ? oldScore : newScore;

		review.update(nextScore, imageUrl, description);
		reviewRepository.save(review);

		FestaStatistic stat = festaStatisticRepository.findById(festaId)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.FESTA_NOT_FOUND));
		stat.updateReview(nextScore, oldScore);
		festaStatisticRepository.save(stat);
	}

	@Transactional
	public void deleteReview(Long memberId, Long festaId) {
		ReviewId id = new ReviewId(memberId, festaId);
		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.REVIEW_NOT_FOUND));

		double score = review.getScore() == null ? 0.0 : review.getScore();

		reviewRepository.delete(review);

		FestaStatistic stat = festaStatisticRepository.findById(festaId)
			.orElseThrow(() -> new OsaekException(ServerErrorCode.FESTA_NOT_FOUND));
		stat.removeReview(score);
		festaStatisticRepository.save(stat);
	}

	@Transactional
	public ReviewListResponseDto getReviewsByFesta(Long festaId, int page, int size, String sort) {
		if (!festaRepository.existsById(festaId)) {
			throw new OsaekException(ServerErrorCode.FESTA_NOT_FOUND);
		}

		Sort sortSpec = switch (sort == null ? "latest" : sort) {
			case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
			case "highest" -> Sort.by(Sort.Direction.DESC, "score");
			case "lowest" -> Sort.by(Sort.Direction.ASC, "score");
			default -> Sort.by(Sort.Direction.DESC, "createdAt");
		};

		page = Math.max(0, page);
		size = Math.max(1, Math.min(size, 50));

		Pageable pageable = PageRequest.of(page, size, sortSpec);
		Page<ReviewResponseDto> reviewPage = reviewRepository.findByFesta_FestaId(festaId, pageable)
			.map(review -> {
				Long memberId = review.getId().getMemberId();
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new OsaekException(ClientErrorCode.USER_ID_NOT_FOUND));
				return ReviewResponseDto.of(review, member.getNickname(), member.getProfile());
			});

		return ReviewListResponseDto.from(reviewPage);
	}
}
