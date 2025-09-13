package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

@Data
public class CreateTemplateDayRequest {

  @NotBlank(message = "Tên ngày tập không được để trống")
  @Size(max = 100, message = "Tên ngày tập không được vượt quá 100 ký tự")
  private String dayName;

  @NotNull(message = "Thứ tự ngày tập là bắt buộc")
  @Min(value = 1, message = "Thứ tự ngày tập tối thiểu là 1")
  private Integer dayOrder;

  @Min(value = 1, message = "Ngày trong tuần từ 1-7")
  @Max(value = 7, message = "Ngày trong tuần từ 1-7")
  private Integer dayOfWeek;

  @NotNull(message = "Thời gian tập là bắt buộc")
  @Min(value = 15, message = "Thời gian tập tối thiểu là 15 phút")
  private Integer durationMinutes;

  private String notes;

  @Valid
  @NotEmpty(message = "Mỗi ngày tập phải có ít nhất 1 bài tập")
  private List<CreateTemplateItemRequest> templateItems;
}
