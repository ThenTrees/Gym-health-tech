package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.thentrees.gymhealthtech.common.GoalStatus;
import com.thentrees.gymhealthtech.common.ObjectiveType;
import com.thentrees.gymhealthtech.model.Goal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoalResponse {
  private String id;
  private String userId;
  private ObjectiveType objective;
  private Integer sessionsPerWeek;
  private Integer sessionMinutes;
  private JsonNode preferences;
  private LocalDateTime startedAt;
  private LocalDate endedAt;
  private LocalDateTime createdAt;
  private GoalStatus status; // ACTIVE, COMPLETED, PAUSED

  // Additional computed fields
  private Integer estimatedCaloriesPerSession;
  private String difficultyAssessment;
  private List<String> recommendedEquipment;
  private String healthSafetyNotes;

  public static GoalResponse from(Goal goal) {
    return GoalResponse.builder()
        .id(goal.getId().toString())
        .userId(goal.getUser().getId().toString())
        .objective(goal.getObjective())
        .sessionsPerWeek(goal.getSessionsPerWeek())
        .sessionMinutes(goal.getSessionMinutes())
        .preferences(goal.getPreferences())
        .startedAt(goal.getStartedAt())
        .endedAt(goal.getEndedAt())
        .createdAt(goal.getCreatedAt())
        .status(goal.getEndedAt() == null ? GoalStatus.ACTIVE : GoalStatus.COMPLETED)
        .estimatedCaloriesPerSession(goal.getEstimatedCaloriesPerSession())
        .difficultyAssessment(goal.getDifficultyAssessment())
        .recommendedEquipment(goal.getRecommendedEquipment())
        .healthSafetyNotes(goal.getHealthSafetyNotes())
        .build();
  }
}
