package com.oseak.myFestaBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oseak.myFestaBackend.entity.DevPickFesta;

public interface DevPickFestaRepository extends JpaRepository<DevPickFesta, Long> {
	@Query(value = "SELECT * FROM dev_pick_festa ORDER BY RAND() LIMIT :count", nativeQuery = true)
	List<DevPickFesta> pickRandom(@Param("count") int count);
}