package com.thentrees.gymhealthtech.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class PlanDayResponse {
  private UUID id;
  private Integer dayIndex;
  private String splitName;
  private LocalDate scheduledDate;
  private OffsetDateTime createdAt;

  // Computed fields
  private Integer totalExercises;
  private Integer estimatedDurationMinutes;
  private Boolean isCompleted;
  private LocalDate lastCompletedDate;

  // Detailed plan items (only when fetching day details)
  private List<PlanItemResponse> planItems;
}
