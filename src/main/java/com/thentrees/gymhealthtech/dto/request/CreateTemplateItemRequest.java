package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateTemplateItemRequest {

  @NotNull(message = "Exercise ID là bắt buộc")
  private UUID exerciseId;

  @NotNull(message = "Thứ tự bài tập là bắt buộc")
  @Min(value = 1, message = "Thứ tự bài tập tối thiểu là 1")
  private Integer itemOrder;

  @NotNull(message = "Số sets là bắt buộc")
  @Min(value = 1, message = "Số sets tối thiểu là 1")
  @Max(value = 10, message = "Số sets tối đa là 10")
    private Integer sets;

  @NotBlank(message = "Số reps là bắt buộc")
  @Size(max = 50, message = "Số reps không được vượt quá 50 ký tự")
  private Integer reps;

  @Min(value = 0, message = "Thời gian nghỉ không được âm")
  private Integer restSeconds;

  private String notes;
}
