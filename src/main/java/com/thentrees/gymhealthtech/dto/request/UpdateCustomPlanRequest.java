package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCustomPlanRequest {

  @Size(max = 120, message = "Tên kế hoạch không được vượt quá 120 ký tự")
  private String title;

  private Integer cycleWeeks;

  private String notes;

  private String status; // ACTIVE, PAUSED, COMPLETED, PAUSE
}
