package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionSetResponse {
  private UUID id;
  private UUID sessionId;
  private UUID exerciseId;
  private String exerciseName;
  private Integer setIndex;
  private JsonNode planned; // What was planned
  private JsonNode actual; // What was actually performed
  private LocalDateTime createdAt;
  private LocalDateTime completedAt;

  // Computed fields
  private Boolean isCompleted;
  private Boolean isSkipped;
  private String performanceComparison; // "above_plan", "as_planned", "below_plan"
  private Integer volume; // weight * reps for this set
}
