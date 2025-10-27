package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class PlanSummaryResponse {
  private UUID id;
  private String title;
  private PlanSourceType source;
  private PlanStatusType status;
  private Integer cycleWeeks;
  private LocalDateTime createdAt;

  // Summary stats
  private Integer totalExercises;
  private Integer totalDays;
  private Integer completedSessions;
  private Double progressPercentage;
  private LocalDate lastWorkoutDate;
  private LocalDate nextScheduledDate;

  // Quick preview
  private List<String> mainMuscleGroups;
  private Integer estimatedWeeklyHours;
  private String difficultyLevel; // "beginner", "intermediate", "advanced"
}
