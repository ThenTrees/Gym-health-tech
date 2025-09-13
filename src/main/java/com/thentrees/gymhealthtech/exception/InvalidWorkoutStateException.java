package com.thentrees.gymhealthtech.exception;

public class InvalidWorkoutStateException extends BusinessException {
  public InvalidWorkoutStateException(String message) {
    super(message, "INVALID_WORKOUT_STATE");
  }
}
