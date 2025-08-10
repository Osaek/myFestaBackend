package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oseak.myFestaBackend.entity.Area;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
}
