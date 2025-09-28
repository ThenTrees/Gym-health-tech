package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionResponse {
  private UUID id;
  private UUID planDayId;
  private String planDayName;
  private LocalDateTime startedAt;
  private LocalDateTime endedAt;
  private SessionStatus status;
  private Integer sessionRpe; // Rate of Perceived Exertion (1-10)
  private String notes;
  private LocalDateTime createdAt;
  // Computed fields
  private Integer durationMinutes;
  private Integer totalSets;
  private Integer completedSets;
  private Double completionPercentage;
  private Integer totalVolume; // Sum of (weight * reps)

  // Session sets (exercises performed)
  private List<SessionSetResponse> sessionSets;
  //
  //  // Plan reference for comparison
  private List<PlanItemResponse> plannedItems;
}
