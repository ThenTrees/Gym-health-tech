package com.thentrees.gymhealthtech.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportFoodResponse {
  private boolean success;
  private String message;
  private Integer totalRows;
  private Integer successCount;
  private Integer failCount;
  private java.util.List<String> errors;

  public static ImportFoodResponse error(String message) {
    return ImportFoodResponse.builder().success(false).message(message).build();
  }
}
