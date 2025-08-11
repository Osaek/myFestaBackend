package com.oseak.myFestaBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oseak.myFestaBackend.entity.SubArea;
import com.oseak.myFestaBackend.entity.SubAreaId;

public interface SubAreaRepository extends JpaRepository<SubArea, SubAreaId> {
	List<SubArea> findByIdAreaCode(Integer areaCode);
}
