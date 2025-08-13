package com.oseak.myFestaBackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.Story;

public interface StoryRepository extends JpaRepository<Story, Long> {
	Optional<Story> findByStoryIdAndIsDeletedFalse(Long storyId);
}
