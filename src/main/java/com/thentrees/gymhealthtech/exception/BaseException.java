package com.thentrees.gymhealthtech.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
  private final String errorCode;
  private final Map<String, Object> metadata;

  public BaseException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.metadata = null;
  }

  public BaseException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.metadata = null;
  }

  public BaseException(String message, String errorCode, Map<String, Object> metadata) {
    super(message);
    this.errorCode = errorCode;
    this.metadata = metadata;
  }
}
