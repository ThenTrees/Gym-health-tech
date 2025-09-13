package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class PlanResponse {
  private UUID id;
  private String title;
  private PlanSourceType source;
  private Integer cycleWeeks;
  private PlanStatusType status;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // Related data
  private GoalResponse goal;
  private Integer totalDays;
  private Integer totalExercises;
  private Integer completedSessions;
  private Double progressPercentage;

  // Detailed plan days (only when fetching plan details)
  private List<PlanDayResponse> planDays;
}
