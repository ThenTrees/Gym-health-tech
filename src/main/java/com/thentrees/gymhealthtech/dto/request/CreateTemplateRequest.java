package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.enums.ObjectiveType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

@Data
public class CreateTemplateRequest {

  @NotBlank(message = "Tên template không được để trống")
  @Size(max = 200, message = "Tên template không được vượt quá 200 ký tự")
  private String name;

  @NotNull(message = "Mục tiêu tập luyện là bắt buộc")
  private ObjectiveType objective;

  @NotNull(message = "Số tuần tập luyện là bắt buộc")
  @Min(value = 1, message = "Số tuần tập luyện tối thiểu là 1")
  @Max(value = 52, message = "Số tuần tập luyện tối đa là 52")
  private Integer durationWeeks;

  @NotNull(message = "Số buổi tập/tuần là bắt buộc")
  @Min(value = 1, message = "Số buổi tập/tuần tối thiểu là 1")
  @Max(value = 7, message = "Số buổi tập/tuần tối đa là 7")
  private Integer sessionsPerWeek;

  @NotNull(message = "Thời gian mỗi buổi tập là bắt buộc")
  @Min(value = 15, message = "Thời gian tập tối thiểu là 15 phút")
  @Max(value = 180, message = "Thời gian tập tối đa là 180 phút")
  private Integer sessionDurationMinutes;

  private String thumbnailUrl;
  private Boolean isFeatured = false;

  @Valid
  @NotEmpty(message = "Template phải có ít nhất 1 ngày tập")
  private List<CreateTemplateDayRequest> templateDays;
}
