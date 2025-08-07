package com.oseak.myFestaBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.SubRegion;
import com.oseak.myFestaBackend.entity.SubRegionId;

public interface SubRegionRepository extends JpaRepository<SubRegion, SubRegionId> {
	List<SubRegion> findByIdRegionCode(Integer regionCode);
}
