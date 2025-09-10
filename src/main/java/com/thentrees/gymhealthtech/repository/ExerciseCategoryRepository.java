package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.ExerciseCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, String> {
  @Query("SELECT c FROM ExerciseCategory c LEFT JOIN FETCH c.exercises WHERE c.code = :code")
  Optional<ExerciseCategory> findByCodeWithExercises(@Param("code") String name);

  @Query("SELECT c FROM ExerciseCategory c LEFT JOIN FETCH c.exercises WHERE c.name = :name")
  Optional<ExerciseCategory> findByNameWithExercises(@Param("name") String name);
}
