package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.common.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for searching sessions with filters and sorting options. - status: Filter sessions by
 * their status (e.g., IN_PROGRESS, COMPLETED, PAUSED, CANCELED). - keyword: Search keyword to
 * filter sessions based on notes content. - sortBy: Field to sort the results by (e.g.,
 * "startedAt", "endedAt").
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SessionSearchRequest {
  private SessionStatus status; // IN_PROGRESS, COMPLETED, PAUSED, CANCELED
  private String keyword; // search in notes

  private Integer page; // page number for pagination
  private Integer size; // page size for pagination
  private String sortBy; // "startedAt", "endedAt"
  private String sortOrder; // "asc", "desc"
}
