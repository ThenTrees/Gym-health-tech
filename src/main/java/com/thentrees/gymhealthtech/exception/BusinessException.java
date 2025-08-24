package com.thentrees.gymhealthtech.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class BusinessException extends BaseException {
  public BusinessException(String message) {
    super(message, "BUSINESS_ERROR");
  }

  public BusinessException(String message, String errorCode) {
    super(message, errorCode);
  }

  public BusinessException(String message, String errorCode, Map<String, Object> metadata) {
    super(message, errorCode, metadata);
  }
}
