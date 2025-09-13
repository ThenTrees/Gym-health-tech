package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdatePlanItemRequest {

  private UUID exerciseId; // Allow changing exercise

  @Min(value = 1, message = "Thứ tự bài tập tối thiểu là 1")
  private Integer itemIndex;

  private CreateCustomPlanItemRequest.PlanItemPrescription prescription;

  private String notes;
}
