package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.PlanItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanItemRepository extends JpaRepository<PlanItem, UUID> {
  Optional<PlanItem> findByIdAndPlanDayIdAndPlanDayPlanIdAndPlanDayPlanUserId(
      UUID planItemId, UUID planDayId, UUID planId, UUID userId);

  List<PlanItem> findByPlanDay_IdOrderByItemIndexAsc(UUID planDayId);
}
