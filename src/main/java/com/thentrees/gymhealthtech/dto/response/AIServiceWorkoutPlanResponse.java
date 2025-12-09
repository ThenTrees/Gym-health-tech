package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Wrapper class that matches the actual JSON structure returned by AI Service
 * This class is used to deserialize the JSON response, then map to GeneratorWorkoutPlanResponse
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIServiceWorkoutPlanResponse {
  private Boolean success;
  private String message;
  private WorkoutPlanData data;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WorkoutPlanData {
    private WorkoutPlan workoutPlan;
    private Long startTime;  // Can be number from AI service
    private Integer generationTime;
    private Integer totalExercises;
    private Integer avgSessionDuration;
    private Object aiMetadata;  // Ignore complex metadata
    private Object generationParams;  // Ignore generation params

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
      private List<PlanDayData> planDays;

      @Data
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class PlanDayData {
        private Integer dayIndex;
        private String scheduledDate;
        private String splitName;
        private List<PlanItemData> planItems;
        private Integer totalDuration;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PlanItemData {
          private ExerciseData exercise;
          private Integer itemIndex;
          private Object prescription;  // Can be complex object
          private String notes;

          @Data
          @JsonIgnoreProperties(ignoreUnknown = true)
          public static class ExerciseData {
            private String id;
            private String slug;
            private String name;
            private Object primaryMuscle;
            private Object equipment;
            private String bodyPart;
            private Object exerciseCategory;
            private Integer difficultyLevel;
            private String instructions;
            private String safetyNotes;
            private String thumbnailUrl;
            private Object benefits;
            private List<String> tags;
            private List<String> alternativeNames;
            private List<Object> secondaryMuscles;
          }
        }
      }
    }
  }
}

