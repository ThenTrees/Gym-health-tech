package com.thentrees.gymhealthtech.dto.response;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeeklySummaryResponse {
  private LocalDate weekStart;
  private LocalDate weekEnd;
  private int totalSessions;
  private int completedSessions;
  private int totalSets;
  private int completedSets;
  private double avgCompletionPercentage;
  private int totalVolume;
  private int totalDurationMinutes;
  private String mostTrainedDayName;
  private List<SessionResponse> dailySummaries;
}
