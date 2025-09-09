package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Exercise;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.thentrees.gymhealthtech.model.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExerciseRepository
    extends JpaRepository<Exercise, UUID>, JpaSpecificationExecutor<Exercise> {
  Optional<Exercise> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, UUID id);
  List<Exercise> findByExerciseCategory(ExerciseCategory category);

}
