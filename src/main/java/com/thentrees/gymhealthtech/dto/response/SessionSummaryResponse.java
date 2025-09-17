package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.SessionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionSummaryResponse {
  private UUID id;
  private UUID planDayId;
  private String planTitle;
  private String planDayName;
  private OffsetDateTime startedAt;
  private OffsetDateTime endedAt;
  private SessionStatus status;
  private BigDecimal sessionRpe;
  private Integer durationMinutes;
  private Integer totalVolume;
  private Integer completedSets;
  private Integer totalPlannedSets;
  private Double completionPercentage;

  // Performance summary
  private Map<String, Integer> exerciseVolumes; // exercise name -> volume
  private Map<String, Object> personalRecords; // Any PRs achieved
  private String overallPerformance; // "excellent", "good", "average", "poor"
}
