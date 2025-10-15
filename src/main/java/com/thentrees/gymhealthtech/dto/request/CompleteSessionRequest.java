package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CompleteSessionRequest {

  private LocalDateTime endTime; // Optional, defaults to now

  @Min(value = 1, message = "Session RPE từ 1-10")
  @Max(value = 10, message = "Session RPE từ 1-10")
  private Integer sessionRpe;

  private String notes; // Post-workout notes

  private String workoutFeeling; // "great", "good", "okay", "poor", "terrible"

  // Optional metrics
  private Integer heartRateAvg;
  private Integer heartRateMax;
  private Integer caloriesBurned;
}
