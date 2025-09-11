package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.common.GoalStatus;
import com.thentrees.gymhealthtech.model.Goal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
  List<Goal> findByUserIdAndEndedAtIsNull(UUID userId);
  // Lấy tất cả goals của user (bao gồm completed)
  @Query(
      "SELECT g FROM Goal g WHERE g.user.id = :userId AND g.isDeleted = false ORDER BY g.createdAt DESC")
  List<Goal> findAllByUserIdIncludingCompleted(@Param("userId") UUID userId);

  // Lấy goals của user (loại trừ completed)
  @Query(
      "SELECT g FROM Goal g WHERE g.user.id = :userId AND g.status != :completedStatus AND g.isDeleted = false ORDER BY g.createdAt DESC")
  List<Goal> findAllByUserIdExcludingCompleted(
      @Param("userId") UUID userId, @Param("completedStatus") GoalStatus completedStatus);

  // Lấy goal đang active
  @Query(
      "SELECT g FROM Goal g WHERE g.user.id = :userId AND g.status = :activeStatus AND g.isDeleted = false")
  Optional<Goal> findActiveGoalByUserId(
      @Param("userId") UUID userId, @Param("activeStatus") GoalStatus activeStatus);
}
