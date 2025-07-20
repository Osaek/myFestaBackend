package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oseak.myFestaBackend.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
