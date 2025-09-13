package com.thentrees.gymhealthtech.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thentrees.gymhealthtech.common.ObjectiveType;
import com.thentrees.gymhealthtech.exception.HealthSafetyException;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.*;

@Data
@Valid
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGoalRequest {
  @NotNull(message = "Objective is required")
  @Enumerated(EnumType.STRING)
  private ObjectiveType objective;

  @NotNull(message = "Sessions per week is required")
  @Min(value = 1, message = "Minimum 1 session per week")
  @Max(value = 7, message = "Maximum 7 sessions per week")
  private Integer sessionsPerWeek;

  @NotNull(message = "Session duration is required")
  @Min(value = 15, message = "Minimum 15 minutes per session")
  @Max(value = 180, message = "Maximum 180 minutes per session")
  private Integer sessionMinutes;

  @Valid private GoalPreferences preferences;

  @FutureOrPresent(message = "Start date cannot be in the past")
  private LocalDateTime startDate;

  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class GoalPreferences {
    // Weight Loss specific
    private Double targetWeightLossKg;
    private Double currentWeightKg;
    private Integer timelineWeeks;

    // Muscle Gain specific
    private Double targetMuscleGainKg;
    private List<String> focusMuscleGroups;
    private String trainingStyle;

    // Endurance specific
    private String targetEvent;
    private String currentFitnessLevel;
    private Double weeklyMileageGoal;

    // Common preferences
    private List<String> preferredExerciseTypes;
    private String intensityPreference;
    private List<String> availableEquipment;
    private String dietaryApproach;
    private List<String> healthConditions;
    private String experienceLevel;

    // Validation method
    public void validate(ObjectiveType objective) {
      switch (objective) {
        case LOSE_FAT:
          validateWeightLossPreferences();
          break;
        case GAIN_MUSCLE:
          validateMuscleGainPreferences();
          break;
        case ENDURANCE:
          validateEndurancePreferences();
          break;
        case MAINTAIN:
          validateMaintenancePreferences();
          break;
      }
    }

    private void validateWeightLossPreferences() {
      if (targetWeightLossKg != null && (targetWeightLossKg <= 0 || targetWeightLossKg > 50)) {
        throw new HealthSafetyException("Target weight loss must be between 0.1-50 kg");
      }
      if (timelineWeeks != null && timelineWeeks < 4) {
        throw new HealthSafetyException("Minimum timeline is 4 weeks for safe weight loss");
      }
      // Safe weight loss rate check (max 1kg per week)
      if (targetWeightLossKg != null && timelineWeeks != null) {
        double maxSafeWeightLoss = timelineWeeks * 1.0; // 1kg per week max
        if (targetWeightLossKg > maxSafeWeightLoss) {
          throw new HealthSafetyException("Target weight loss exceeds safe rate of 1kg per week");
        }
      }
    }

    private void validateMuscleGainPreferences() {
      if (targetMuscleGainKg != null && (targetMuscleGainKg <= 0 || targetMuscleGainKg > 20)) {
        throw new HealthSafetyException("Target muscle gain must be between 0.1-20 kg");
      }
      // Realistic muscle gain rate (max 0.5kg per month)
      if (targetMuscleGainKg != null && timelineWeeks != null) {
        double maxRealisticGain = (timelineWeeks / 4.0) * 2.0; // 0.5kg per week max
        if (targetMuscleGainKg > maxRealisticGain) {
          throw new HealthSafetyException("Target muscle gain exceeds realistic rate");
        }
      }
    }

    private void validateEndurancePreferences() {
      if (targetEvent != null && !isValidEnduranceEvent(targetEvent)) {
        throw new IllegalArgumentException("Invalid target event specified");
      }
    }

    private void validateMaintenancePreferences() {
      // Maintenance goals are more flexible, basic validation only
    }

    private boolean isValidEnduranceEvent(String event) {
      List<String> validEvents =
          Arrays.asList(
              "5k_run",
              "10k_run",
              "half_marathon",
              "marathon",
              "cycling_50k",
              "cycling_100k",
              "triathlon",
              "swimming_1k");
      return validEvents.contains(event.toLowerCase());
    }
  }
}
