package com.thentrees.gymhealthtech.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class RestTimerResponse {
  private UUID sessionSetId;
  private Integer plannedRestSeconds;
  private OffsetDateTime restStartTime;
  private Integer remainingSeconds;
  private Boolean isActive;
  private String nextExercise; // Preview of next exercise
}
