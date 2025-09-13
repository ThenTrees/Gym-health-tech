package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class CreateCustomPlanDayRequest {

  @NotNull(message = "Chỉ số ngày là bắt buộc")
  @Min(value = 1, message = "Chỉ số ngày tối thiểu là 1")
  private Integer dayIndex;

  @Size(max = 50, message = "Tên phân chia không được vượt quá 50 ký tự")
  private String splitName; // "Push Day", "Pull Day", "Legs", etc.

  private LocalDate scheduledDate; // Optional - specific date for this workout

  @Valid
  @NotEmpty(message = "Mỗi ngày tập phải có ít nhất 1 bài tập")
  private List<CreateCustomPlanItemRequest> planItems;
}
