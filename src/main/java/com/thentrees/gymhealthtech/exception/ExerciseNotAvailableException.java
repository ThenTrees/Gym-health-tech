package com.thentrees.gymhealthtech.exception;

public class ExerciseNotAvailableException extends BusinessException {
  public ExerciseNotAvailableException(String message) {
    super(message, "EXERCISE_NOT_AVAILABLE");
  }
}
