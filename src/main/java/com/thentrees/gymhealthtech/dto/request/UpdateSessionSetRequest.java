package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateSessionSetRequest {

  @NotNull(message = "Số reps thực tế là bắt buộc")
  @Min(value = 0, message = "Số reps không được âm")
  private Integer actualReps;

  @DecimalMin(value = "0.0", message = "Trọng lượng không được âm")
  private BigDecimal actualWeight;

  @Min(value = 1, message = "RPE từ 1-10")
  @Max(value = 10, message = "RPE từ 1-10")
  private Integer rpe; // Rate of Perceived Exertion

  @Min(value = 0, message = "Thời gian nghỉ không được âm")
  private Integer restSeconds;

  private String notes;

  @Builder.Default
  private Boolean isSkipped = false; // Mark set as skipped

  // Time tracking
  private Integer setDurationSeconds; // How long the set took
}
