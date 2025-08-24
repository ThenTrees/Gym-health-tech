package com.thentrees.gymhealthtech.exception;

public class UnauthorizedException extends BaseException {
  public UnauthorizedException(String message) {
    super(message, "UNAUTHORIZED");
  }
}
