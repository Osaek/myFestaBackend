package com.oseak.myFestaBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.oseak.myFestaBackend.entity.Festa;

public interface FestaRepository extends JpaRepository<Festa, Long>, JpaSpecificationExecutor<Festa> {
	Optional<Festa> findByFestaName(String festaName);

	Optional<Festa> findByFestaNameAndFestaStartAt(String festaName, LocalDate festaStartAt);

	List<Festa> findAllByFestaIdIn(List<Long> festaIds);

	//Haversine공식
	@Query(value = """
		SELECT f.*
		FROM festa f
		WHERE f.festa_status IN ('SCHEDULED','ONGOING')
			AND f.latitude  IS NOT NULL
		    AND f.longitude IS NOT NULL
		    AND (
		  		6371 * acos(
		    		cos(radians(:latitude)) * cos(radians(f.latitude)) *
		    		cos(radians(f.longitude) - radians(:longitude)) +
		    		sin(radians(:latitude)) * sin(radians(f.latitude))
		  			)
			) <= :distance
		ORDER BY
		             6371 * acos(
		               cos(radians(:latitude)) * cos(radians(f.latitude)) *
		               cos(radians(f.longitude) - radians(:longitude)) +
		               sin(radians(:latitude)) * sin(radians(f.latitude))
		             ) ASC
		""", nativeQuery = true)
	List<Festa> findByDistance(double latitude, double longitude, double distance);

	@Query(value = """
		SELECT * 
		FROM festa
		WHERE festa_status IN ('SCHEDULED','ONGOING')
		ORDER BY RAND()
		LIMIT :limit
		""", nativeQuery = true)
	List<Festa> findRandomFestas(int limit);

}