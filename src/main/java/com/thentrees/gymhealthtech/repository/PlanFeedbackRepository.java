package com.thentrees.gymhealthtech.repository;



import com.thentrees.gymhealthtech.model.PlanFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PlanFeedbackRepository extends JpaRepository<PlanFeedback, UUID> {
  Optional<PlanFeedback> findByPlanIdAndUserId(UUID planId, UUID userId);
  List<PlanFeedback> findByPlanId(UUID planId);
}
