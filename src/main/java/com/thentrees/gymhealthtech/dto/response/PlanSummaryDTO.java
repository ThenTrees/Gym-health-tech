package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import lombok.Data;

/** Summary DTO for Plan shared in posts */
@Data
public class PlanSummaryDTO {
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
