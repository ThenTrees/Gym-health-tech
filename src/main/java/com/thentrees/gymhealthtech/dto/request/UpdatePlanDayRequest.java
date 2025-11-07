package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdatePlanDayRequest {

  @Size(max = 50, message = ValidationMessages.NAME_SLUG_EXCEED_50_CHAR)
  private String splitName;

  private LocalDate scheduledDate;

  private String notes;

  private Integer dayIndex; // Allow reordering days
}
