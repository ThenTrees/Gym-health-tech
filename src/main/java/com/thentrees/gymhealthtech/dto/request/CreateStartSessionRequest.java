package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateStartSessionRequest {
  @NotNull(message = "Plan day Id  is required")
  private UUID planDayId;

  @Builder.Default
  private LocalDateTime startTime = LocalDateTime.now(); // Optional, defaults to now

  private String notes;
  private Integer estimatedDurationMinutes;
}
