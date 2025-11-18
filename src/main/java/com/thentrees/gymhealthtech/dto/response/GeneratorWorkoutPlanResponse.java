package com.thentrees.gymhealthtech.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thentrees.gymhealthtech.model.PlanDay;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratorWorkoutPlanResponse {
  private String message;
  private String status;
  private Metadata metadata;

  @Data
  public static class Metadata {
    private WorkoutPlan workoutPlan;
    private String startTime;
    private String generationTime;
    private int totalExercises;
    private int avgSessionDuration;

    @Data
    public static class WorkoutPlan {
      private String id;
      private String userId;
      private String goalId;
      private String title;
      private String description;
      private int totalWeeks;
      private int totalDays;
      private String createdAt;
      private String endDate;
      @JsonIgnoreProperties(ignoreUnknown = true)
      private List<PlanDay> planDays;

      @JsonIgnore
      private Object aiMetadata;

      @JsonIgnore
      private Object generationParams;
    }
  }
}
