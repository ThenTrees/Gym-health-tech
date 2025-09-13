package com.thentrees.gymhealthtech.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
  private List<T> content;
  private Pagination pagination;

  public static <T> PagedResponse<T> of(Page<T> page) {
    return PagedResponse.<T>builder()
        .content(page.getContent())
        .pagination(
            Pagination.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build())
        .build();
  }

  public static <T> PagedResponse<T> of(Page<T> page, String sortBy, String sortDirection) {
    return PagedResponse.<T>builder()
        .content(page.getContent())
        .pagination(
            Pagination.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build())
        .build();
  }
}
