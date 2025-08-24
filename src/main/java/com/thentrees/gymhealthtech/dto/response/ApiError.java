package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
public class ApiError {
  private String code;
  private String message;
  private String details;
  private List<FieldError> fieldErrors;
  private Map<String, Object> metadata;
  private String traceId;
}
