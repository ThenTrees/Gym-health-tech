package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.PlanDay;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanDayRepository extends JpaRepository<PlanDay, UUID> {
  Optional<PlanDay> findByIdAndPlanIdAndPlanUserId(UUID planDayId, UUID planId, UUID userId);

  Optional<PlanDay> findByIdAndPlanUserId(UUID planDayId, UUID userId);
}
