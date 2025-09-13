package com.thentrees.gymhealthtech.exception;

public class HealthSafetyException extends BaseException {
  public HealthSafetyException(String message) {
    super(message, "HEALTH_SAFETY");
  }
}
