package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.SessionSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionSetRepository extends JpaRepository<SessionSet, UUID> {
  Optional<SessionSet> findByIdAndSessionUserId(UUID sessionSetId, UUID userId);
}
