package com.oseak.myFestaBackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.Review;
import com.oseak.myFestaBackend.entity.ReviewId;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
	Page<Review> findByFesta_FestaId(Long festaId, Pageable pageable);
	Page<Review> findById_MemberId(Long memberId, Pageable pageable);
}
