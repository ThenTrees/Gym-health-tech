package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.model.Session;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.thentrees.gymhealthtech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository
    extends JpaRepository<Session, UUID>, JpaSpecificationExecutor<Session> {
  boolean existsByPlanDayIdAndStatus(UUID planDayId, SessionStatus status);

  @Query(
      "SELECT COUNT(s) FROM Session s WHERE s.planDay.plan.id = :planId AND s.status = 'COMPLETED'")
  Integer countCompletedSessionsByPlanId(@Param("planId") UUID planId);

  @Query(
      "SELECT COUNT(s) FROM Session s JOIN s.sessionSets ss WHERE ss.exercise.id IN "
          + "(SELECT pi.exercise.id FROM PlanItem pi WHERE pi.id = :planItemId) AND s.status = 'COMPLETED'")
  Integer countCompletedSessionsByPlanItemId(@Param("planItemId") UUID planItemId);

  boolean existsByPlanDayPlanIdAndStatus(UUID planId, SessionStatus status);

  boolean existsBySessionSets_PlanItem_IdAndStatus(UUID planItemId, SessionStatus status);

  Optional<Session> findByUserIdAndStatus(UUID userId, SessionStatus status);

  Optional<Session> findActiveSessionByUserId(UUID userId);

  boolean existsByPlanDayIdAndStatusAndEndedAtGreaterThanEqualAndEndedAtLessThan(
      UUID planDayId, SessionStatus status, LocalDateTime from, LocalDateTime to);

  Optional<Session> findByIdAndUserId(UUID sessionId, UUID userId);

  @Query(
      "SELECT s FROM Session s "
          + "LEFT JOIN FETCH s.sessionSets ss "
          + "LEFT JOIN FETCH ss.exercise "
          + "WHERE s.id = :sessionId AND s.user.id = :userId")
  Optional<Session> findByIdAndUserIdWithSets(
      @Param("sessionId") UUID sessionId, @Param("userId") UUID userId);

  @Query(
    "SELECT s FROM Session s "
      + "LEFT JOIN FETCH s.sessionSets ss "
      + "LEFT JOIN FETCH ss.exercise "
      + "WHERE s.planDay.id = :planDayId AND s.user.id = :userId")
  Optional<Session> findByPlanDayIdAndUserIdWithSets(
    @Param("planDayId") UUID planDayId, @Param("userId") UUID userId);

  List<Session> findByUserAndStartedAtBetween(User user, LocalDateTime startedAt, LocalDateTime startedAt2);

  List<Session> findByPlanDayPlanIdAndUserId(UUID planId, UUID userId);

  @Query("""
    SELECT s FROM Session s
    WHERE s.startedAt BETWEEN :startOfMonth AND :endOfMonth
      AND s.user.id = :userId
""")
  List<Session> findByUserAndAllSessionsInCurrentMonth(@Param("userId") UUID userId,
                                                       @Param("startOfMonth") LocalDateTime startOfMonth,
                                                       @Param("endOfMonth") LocalDateTime endOfMonth);
}
