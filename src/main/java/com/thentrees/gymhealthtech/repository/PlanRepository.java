package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.dto.request.PlanSearchRequest;
import com.thentrees.gymhealthtech.model.Plan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
  Optional<Plan> findByIdAndUserId(UUID id, UUID userId);

  @Query(
      "SELECT p FROM Plan p WHERE p.user.id = :userId "
          + "AND (:#{#criteria.query} IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :#{#criteria.query}, '%'))) "
          + "AND (:#{#criteria.sourceTypes} IS NULL OR p.source IN :#{#criteria.sourceTypes}) "
          + "AND (:#{#criteria.statusTypes} IS NULL OR p.status IN :#{#criteria.statusTypes}) ")
  //    "AND (:#{#criteria.} IS NULL OR DATE(p.createdAt) >= :#{#criteria.createdAfter}) " +
  //    "AND (:#{#criteria.createdBefore} IS NULL OR DATE(p.createdAt) <=
  // :#{#criteria.createdBefore})")
  Page<Plan> findPlansWithCriteria(
      @Param("userId") UUID userId,
      @Param("criteria") PlanSearchRequest criteria,
      Pageable pageable);

  List<Plan> findByUserId(UUID userId);
}
