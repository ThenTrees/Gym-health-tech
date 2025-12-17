package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomPlanRequest {
  @NotBlank(message = ValidationMessages.PLAN_NAME_REQUIRE)
  @Size(max = 120, message = ValidationMessages.PLAN_NAME_EXCEED_120_CHAR)
  private String title;

  private UUID goalId;

  @NotNull(message = ValidationMessages.CYCLE_WEEK_REQUIRE)
  @Min(value = 1, message = ValidationMessages.CYCLE_WEEK_MIN)
  @Max(value = 52, message = ValidationMessages.CYCLE_WEEK_MAX)
  private Integer cycleWeeks;

  private String notes;

  @Valid
  @NotEmpty(message = ValidationMessages.PLAN_MUST_INCLUDE_PLAN_DAY)
  private List<CreateCustomPlanDayRequest> planDays;
}
