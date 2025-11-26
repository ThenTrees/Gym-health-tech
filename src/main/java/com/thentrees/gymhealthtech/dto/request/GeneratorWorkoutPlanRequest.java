package com.thentrees.gymhealthtech.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class GeneratorWorkoutPlanRequest {
  private UUID userId;
}
