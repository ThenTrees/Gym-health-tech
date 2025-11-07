package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomPlanItemRequest {

  @NotNull(message = ValidationMessages.EX_ID_REQUIRE)
  private UUID exerciseId;

  @NotNull(message = ValidationMessages.EX_IDX_REQUIRE)
  @Min(value = 1, message = ValidationMessages.EX_IDX_MIN)
  private Integer itemIndex;

  @NotNull(message = ValidationMessages.PARAMETER_EXERCISE_REQUIRE)
  private PlanItemPrescription prescription;

  private String notes;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PlanItemPrescription {
    @NotNull(message = ValidationMessages.SET_REQUIRE)
    @Min(value = 1, message = ValidationMessages.SET_MIN)
    @Max(value = 10, message = ValidationMessages.SET_MAX)
    private Integer sets;

    @NotBlank(message = ValidationMessages.REP_REQUIRE)
    private String reps; // "8-12", "max", "30 seconds"

    @Min(value = 0, message = ValidationMessages.REST_TIME_NOT_NEGATIVE)
    @Builder.Default
    private Integer restSeconds = 60;

    private Double weightKg; // Optional, có thể null nếu không áp dụng

    private Integer rpe; // Optional, có thể null nếu không áp dụng
  }
}
