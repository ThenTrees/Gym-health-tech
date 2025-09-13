package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PlanItemResponse {
  private UUID id;
  private Integer itemIndex;
  private JsonNode prescription;
  private String notes;
  private OffsetDateTime createdAt;

  // Exercise details
  private ExerciseDetailResponse exercise;

  // Performance tracking
  private Integer timesCompleted;
  private JsonNode lastPerformance; // Last recorded actual performance
  private String progressTrend; // "improving", "maintaining", "declining"
}
