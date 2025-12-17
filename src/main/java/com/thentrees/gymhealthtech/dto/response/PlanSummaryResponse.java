package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import lombok.Data;

/** Summary response for Plan shared in posts */
@Data
public class PlanSummaryResponse {
  private String id;
  private String title;
  private String description;
  private PlanSourceType source;
  private PlanStatusType status;
  private Integer cycleWeeks;
  private Integer totalDays;
  private Integer totalExercises;
  private String goalName;
}
