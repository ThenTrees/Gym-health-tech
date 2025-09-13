package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CreateCustomPlanItemRequest {

  @NotNull(message = "Exercise ID là bắt buộc")
  private UUID exerciseId;

  @NotNull(message = "Thứ tự bài tập là bắt buộc")
  @Min(value = 1, message = "Thứ tự bài tập tối thiểu là 1")
  private Integer itemIndex;

  @NotNull(message = "Thông số bài tập là bắt buộc")
  private PlanItemPrescription prescription;

  private String notes;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PlanItemPrescription {
    @NotNull(message = "Số sets là bắt buộc")
    @Min(value = 1, message = "Số sets tối thiểu là 1")
    @Max(value = 10, message = "Số sets tối đa là 10")
    private Integer sets;

    @NotBlank(message = "Số reps là bắt buộc")
    @Size(max = 50, message = "Số reps không được vượt quá 50 ký tự")
    private String reps; // "8-12", "max", "30 seconds"

    @Min(value = 0, message = "Thời gian nghỉ không được âm")
    @Builder.Default
    private Integer restSeconds = 60;
  }
}
