package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomPlanDayRequest {

  @NotNull(message = ValidationMessages.DAY_IDX_REQUIRE)
  @Min(value = 1, message = ValidationMessages.DAY_IDX_MIN)
  private Integer dayIndex;

  @Size(max = 50, message = ValidationMessages.DAY_NAME_EXCEED_50_CHAR)
  private String splitName; // "Push Day", "Pull Day", "Legs", etc.

  private LocalDate scheduledDate; // Optional - specific date for this workout

  @Valid
  @NotEmpty(message = ValidationMessages.DAY_TRAINING_INCLUDE_EXERCISE)
  private List<CreateCustomPlanItemRequest> planItems;
}
