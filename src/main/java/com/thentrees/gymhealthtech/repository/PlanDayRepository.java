package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.PlanDay;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanDayRepository extends JpaRepository<PlanDay, UUID> {
  
  @Query("""
      SELECT DISTINCT pd FROM PlanDay pd
      LEFT JOIN FETCH pd.plan
      LEFT JOIN FETCH pd.planItems pi
      LEFT JOIN FETCH pi.exercise
      WHERE pd.id = :planDayId AND pd.plan.id = :planId AND pd.plan.user.id = :userId
      """)
  Optional<PlanDay> findByIdAndPlanIdAndPlanUserId(
      @Param("planDayId") UUID planDayId, 
      @Param("planId") UUID planId, 
      @Param("userId") UUID userId);

  @Query("""
      SELECT DISTINCT pd FROM PlanDay pd
      LEFT JOIN FETCH pd.plan
      LEFT JOIN FETCH pd.planItems pi
      LEFT JOIN FETCH pi.exercise
      WHERE pd.id = :planDayId AND pd.plan.user.id = :userId
      """)
  Optional<PlanDay> findByIdAndPlanUserId(@Param("planDayId") UUID planDayId, @Param("userId") UUID userId);

  @Query("""
      SELECT DISTINCT pd FROM PlanDay pd
      LEFT JOIN FETCH pd.planItems pi
      LEFT JOIN FETCH pi.exercise
      WHERE pd.plan.id = :planId
      """)
  List<PlanDay> findAllByPlanId(@Param("planId") UUID planId);
}

