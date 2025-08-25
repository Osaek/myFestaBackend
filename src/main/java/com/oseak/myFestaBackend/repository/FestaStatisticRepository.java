package com.oseak.myFestaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.FestaStatistic;

public interface FestaStatisticRepository extends JpaRepository<FestaStatistic, Long> {
}