package com.oseak.myFestaBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.oseak.myFestaBackend.entity.Story;

public interface StoryRepository extends JpaRepository<Story, Long>,
	JpaSpecificationExecutor<Story> {
	List<Story> findAllByIsDeletedTrue();
}
