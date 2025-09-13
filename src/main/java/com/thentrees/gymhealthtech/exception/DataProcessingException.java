package com.thentrees.gymhealthtech.exception;

public class DataProcessingException extends BaseException {
  public DataProcessingException(String message, Throwable cause) {
    super(message, String.valueOf(cause));
  }
}
