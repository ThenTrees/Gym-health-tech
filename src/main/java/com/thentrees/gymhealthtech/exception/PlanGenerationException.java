package com.thentrees.gymhealthtech.exception;

public class PlanGenerationException extends BusinessException {
  public PlanGenerationException(String message) {
    super(message, "PLAN_GENERATION_FAILED");
  }
}
