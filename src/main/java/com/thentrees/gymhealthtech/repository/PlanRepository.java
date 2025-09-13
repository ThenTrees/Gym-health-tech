package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Plan;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
  Optional<Plan> findByIdAndUserId(UUID id, UUID userId);
}
