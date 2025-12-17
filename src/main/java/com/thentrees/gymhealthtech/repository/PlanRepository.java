package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.dto.request.PlanSearchRequest;
import com.thentrees.gymhealthtech.model.Plan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

  @Query("""
      SELECT DISTINCT p FROM Plan p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH p.goal
      LEFT JOIN FETCH p.planDays pd
      LEFT JOIN FETCH pd.planItems pi
      LEFT JOIN FETCH pi.exercise
      WHERE p.id = :id AND p.user.id = :userId
      """)
  Optional<Plan> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

  @Query(
      "SELECT DISTINCT p FROM Plan p "
          + "LEFT JOIN FETCH p.user "
          + "LEFT JOIN FETCH p.goal "
          + "WHERE p.user.id = :userId "
          + "AND (:#{#criteria.query} IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :#{#criteria.query}, '%'))) "
          + "AND (:#{#criteria.sourceTypes} IS NULL OR p.source IN :#{#criteria.sourceTypes}) "
          + "AND (:#{#criteria.statusTypes} IS NULL OR p.status IN :#{#criteria.statusTypes})")
  Page<Plan> findPlansWithCriteria(
      @Param("userId") UUID userId,
      @Param("criteria") PlanSearchRequest criteria,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT p FROM Plan p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH p.goal
      LEFT JOIN FETCH p.planDays pd
      LEFT JOIN FETCH pd.planItems pi
      LEFT JOIN FETCH pi.exercise
      WHERE p.user.id = :userId
      """)
  List<Plan> findByUserId(@Param("userId") UUID userId);

  @EntityGraph(attributePaths = {
    "user",
    "goal",
    "planDays",
    "planDays.planItems",
  })
  Optional<Plan> findById(UUID id);
}
