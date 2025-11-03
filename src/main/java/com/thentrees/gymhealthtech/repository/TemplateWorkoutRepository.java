package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateWorkoutRepository extends JpaRepository<WorkoutTemplate, UUID> {
  Optional<WorkoutTemplate> findByIdAndIsActiveTrue(UUID id);
  List<WorkoutTemplate> findAllByIsActiveTrue();
}
