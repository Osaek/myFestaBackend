package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.Review;
import com.oseak.myFestaBackend.entity.ReviewId;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {

}
