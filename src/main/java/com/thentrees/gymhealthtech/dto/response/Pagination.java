package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;
  private String sortBy;
  private String sortDirection;
}
