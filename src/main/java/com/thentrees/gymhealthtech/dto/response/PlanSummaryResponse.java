package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.enums.DifficultyLevel;
import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanSummaryResponse {
  private UUID id;
  private String title;
  private PlanSourceType source;
  private PlanStatusType status;

  @Builder.Default
  private Integer totalWeeks = 0;
  private LocalDateTime createdAt;
  private LocalDateTime startedAt;
  private LocalDateTime endedAt;

  private double totalCalories;
  private long totalDurationMinutes;
  private int totalSessions;
  private BigDecimal completionRate;
  private BigDecimal avgCaloriesPerSession;
  private BigDecimal avgDurationPerSession;

  // Summary stats
  private Integer totalExercises;
  private Integer totalDays;
  private Integer completedSessions;
  private Double timelineProgressPercentage;
  private LocalDate lastWorkoutDate;
  private LocalDate nextScheduledDate;
  private Integer skippedSessions;
  private Integer missedDays;


  // Quick preview
  private List<String> mainMuscleGroups;
  private Integer estimatedWeeklyHours;
  private DifficultyLevel difficultyLevel; // "beginner", "intermediate", "advanced"
}
