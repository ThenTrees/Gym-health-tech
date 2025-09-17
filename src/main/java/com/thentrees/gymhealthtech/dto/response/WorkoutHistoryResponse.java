package com.thentrees.gymhealthtech.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class WorkoutHistoryResponse {
  private LocalDate date;
  private Integer totalSessions;
  private Integer totalDuration; // minutes
  private Integer totalVolume;
  private Double averageRpe;
  private List<SessionSummaryResponse> sessions;
  private Map<String, Integer> muscleGroupsWorked;
}
