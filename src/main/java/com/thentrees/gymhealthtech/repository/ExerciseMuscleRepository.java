package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.ExerciseMuscle;
import com.thentrees.gymhealthtech.model.ExerciseMuscleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseMuscleRepository extends JpaRepository<ExerciseMuscle, ExerciseMuscleId> {
  @Query(
      "SELECT em FROM ExerciseMuscle em "
          + "JOIN FETCH em.muscle "
          + "WHERE em.exercise.id = :exerciseId "
          + "ORDER BY CASE WHEN em.role = 'PRIMARY' THEN 1 ELSE 2 END, em.muscle.name")
  List<ExerciseMuscle> findByExerciseIdOrderByRole(@Param("exerciseId") UUID exerciseId);

  void deleteByExerciseId(UUID exerciseId);
}
