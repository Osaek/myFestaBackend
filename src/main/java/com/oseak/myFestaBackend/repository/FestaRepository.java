package com.oseak.myFestaBackend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.Festa;

public interface FestaRepository extends JpaRepository<Festa, Long> {
	Optional<Festa> findByFestaName(String festaName);
	Optional<Festa> findByFestaNameAndFestaStartAt(String festaName, LocalDateTime festaStartAt);

}