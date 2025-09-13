package com.thentrees.gymhealthtech.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private boolean success;
  private int status; // mã trạng thái HTTP
  private String message;
  private String errorCode;
  private Object details;
  private Instant timestamp; // thời điểm xảy ra lỗi
  private String traceId; // mã định danh request
}
