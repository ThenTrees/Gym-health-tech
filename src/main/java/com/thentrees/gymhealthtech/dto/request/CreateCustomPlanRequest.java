package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateCustomPlanRequest {
  @NotBlank(message = "Tên kế hoạch tập luyện không được để trống")
  @Size(max = 120, message = "Tên kế hoạch không được vượt quá 120 ký tự")
  private String title;

  private UUID goalId; // Optional - link to existing goal

  @NotNull(message = "Số tuần tập luyện là bắt buộc")
  @Min(value = 1, message = "Số tuần tập luyện tối thiểu là 1")
  @Max(value = 52, message = "Số tuần tập luyện tối đa là 52")
  private Integer cycleWeeks;

  private String notes;

  @Valid
  @NotEmpty(message = "Kế hoạch phải có ít nhất 1 ngày tập")
  private List<CreateCustomPlanDayRequest> planDays;
}
