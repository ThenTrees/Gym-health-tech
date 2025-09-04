package com.thentrees.gymhealthtech.exception;

public class TokenExpiredException extends BaseException {
  public TokenExpiredException(String message) {
    super(message, "TOKEN_EXPIRED");
  }
}
