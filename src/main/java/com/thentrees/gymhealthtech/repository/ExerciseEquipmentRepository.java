package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.ExerciseEquipment;
import com.thentrees.gymhealthtech.model.ExerciseEquipmentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseEquipmentRepository
    extends JpaRepository<ExerciseEquipment, ExerciseEquipmentId> {}
