package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CompleteSessionRequest {

  private LocalDateTime endTime; // Optional, defaults to now

  @DecimalMin(value = "1.0", message = "Session RPE từ 1-10")
  @DecimalMax(value = "10.0", message = "Session RPE từ 1-10")
  private BigDecimal sessionRpe;

  private String notes; // Post-workout notes

  private String workoutFeeling; // "great", "good", "okay", "poor", "terrible"

  // Optional metrics
  private Integer heartRateAvg;
  private Integer heartRateMax;
  private Integer caloriesBurned;
}
