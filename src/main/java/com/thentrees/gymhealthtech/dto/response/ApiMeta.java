package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMeta {
  private Pagination pagination;
  private Map<String, Object> filters;
  private String version;
  private Long executionTime;
}
