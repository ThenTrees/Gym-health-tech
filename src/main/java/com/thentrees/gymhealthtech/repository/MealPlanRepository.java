package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.MealPlan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

  Optional<MealPlan> findByUserIdAndPlanDate(UUID userId, LocalDate planDate);

  List<MealPlan> findByUserIdAndPlanDateBetween(
      UUID userId, LocalDate startDate, LocalDate endDate);
}
