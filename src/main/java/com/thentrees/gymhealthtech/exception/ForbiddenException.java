package com.thentrees.gymhealthtech.exception;

public class ForbiddenException extends BaseException {
  public ForbiddenException(String message) {
    super(message, "FORBIDDEN");
  }
}
