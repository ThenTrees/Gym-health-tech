package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCustomPlanRequest {

  @Size(max = 120, message = ValidationMessages.NAME_SLUG_EXCEED_50_CHAR)
  private String title;

  private Integer cycleWeeks;

  private String notes;

  private String status; // ACTIVE, PAUSED, COMPLETED, PAUSE
}
