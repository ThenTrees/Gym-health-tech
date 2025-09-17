package com.thentrees.gymhealthtech.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionTemplateResponse {
  private UUID planDayId;
  private String planDayName;
  private Integer estimatedDurationMinutes;
  private Integer totalExercises;
  private List<PlannedSetDto> plannedSets;

  @Data
  public static class PlannedSetDto {
    private UUID planItemId;
    private UUID exerciseId;
    private String exerciseName;
    private String exerciseInstructions;
    private String muscleGroup;
    private Integer setNumber;
    private Integer plannedSets;
    private String plannedReps;
    private Double plannedWeight;
    private Integer restSeconds;
    private String notes;
    private String imageUrl;
  }
}
