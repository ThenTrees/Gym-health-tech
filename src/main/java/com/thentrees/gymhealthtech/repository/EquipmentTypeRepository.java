package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, String> {}
