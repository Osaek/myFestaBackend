package com.oseak.myFestaBackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.oseak.myFestaBackend.entity.Festa;

public interface FestaRepository extends JpaRepository<Festa, Long>, JpaSpecificationExecutor<Festa> {
	Optional<Festa> findByFestaName(String festaName);

	Optional<Festa> findByFestaNameAndFestaStartAt(String festaName, LocalDateTime festaStartAt);

	Optional<Festa> findByContentId(Long contentId);

	List<Festa> findAllByContentIdIn(List<Long> contentIds);

	//Haversine공식
	@Query(value = """
		SELECT f.*
		FROM festa f
		WHERE (
		  	6371 * acos(
		    	cos(radians(:latitude)) * cos(radians(f.latitude)) *
		    	cos(radians(f.longitude) - radians(:longitude)) +
		    	sin(radians(:latitude)) * sin(radians(f.latitude))
		  		)
		) <= :distance
		""", nativeQuery = true)
	List<Festa> findByDistance(double latitude, double longitude, double distance);

	@Query(value = "SELECT * FROM festa ORDER BY RAND() LIMIT :limit", nativeQuery = true)
	List<Festa> findRandomFestivals(int limit);

}