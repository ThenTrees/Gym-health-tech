package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thentrees.gymhealthtech.model.PlanItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratorWorkoutPlanResponse {
  private String message;
  private String status;
  private Metadata metadata;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Metadata {
    private WorkoutPlan workoutPlan;
    private String startTime;
    private String generationTime;
    private Integer totalExercises;
    private Integer avgSessionDuration;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkoutPlan {

      private String id;
      private String userId;
      private String goalId;
      private String title;
      private String description;
      private Integer totalWeeks;
      private Integer totalDays;
      private String createdAt;
      private String endDate;

      private List<WorkoutPlan.PlanDay> planDays;

      @JsonIgnore
      private Object aiMetadata;

      @JsonIgnore
      private Object generationParams;

      @Data
      @JsonIgnoreProperties(ignoreUnknown = true)
      @JsonInclude(JsonInclude.Include.NON_NULL)
      public static class PlanDay {

        private Integer dayIndex;
        private String scheduledDate;
        private String splitName;

        private List<PlanItem> planItems;
      }
    }
  }
}
