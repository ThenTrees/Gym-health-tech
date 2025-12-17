package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.UserExercisePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserExercisePreferenceRepository extends JpaRepository<UserExercisePreference, UUID> {
  Optional<UserExercisePreference> findByUserIdAndExerciseId(UUID userId, UUID exerciseId);
  List<UserExercisePreference> findByUserId(UUID userId);


  // Top liked exercises by user
  @Query("SELECT u FROM UserExercisePreference u WHERE u.user = :userId AND u.preferenceScore > 0 ORDER BY u.preferenceScore DESC")
  List<UserExercisePreference> findTopLikedByUser(@Param("userId") UUID userId);
}
