package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateWorkoutDayResponse {
  private String dayName;
  private Integer dayOrder;
  private Integer dayOfWeek;
  private Integer durationMinutes;
  private String notes;
  private Integer totalExercises;
  private List<TemplateItemResponse> templateItems;
}
