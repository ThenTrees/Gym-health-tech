package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdatePlanDayRequest {

  @Size(max = 50, message = "Tên phân chia không được vượt quá 50 ký tự")
  private String splitName;

  private LocalDate scheduledDate;

  private String notes;

  private Integer dayIndex; // Allow reordering days
}
