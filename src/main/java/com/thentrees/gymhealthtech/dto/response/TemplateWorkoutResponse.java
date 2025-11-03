package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateWorkoutResponse {
  private UUID id;
  private String name;
  private String description;
  private String goal;
  private Integer totalWeek;
  private Integer sessionPerWeek;
  private String thumbnailUrl;
  private Integer totalUsed;
  private Integer totalExercise;
  private Boolean isActive;
  private List<TemplateWorkoutDayResponse> templateWorkoutDay;
}
