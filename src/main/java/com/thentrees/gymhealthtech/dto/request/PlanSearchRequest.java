package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
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
public class PlanSearchRequest {
  private String query; // search in title or notes
  private List<PlanSourceType> sourceTypes; // AI, TEMPLATE, CUSTOM
  private List<PlanStatusType> statusTypes; // ACTIVE, COMPLETED, PAUSED
  private List<UUID> goalIds;

  private Boolean hasActiveGoal; //
  private String sortBy; // "createdAt", "title", "progress", "lastActivity"
  private String sortDirection; // "asc", "desc"
}
