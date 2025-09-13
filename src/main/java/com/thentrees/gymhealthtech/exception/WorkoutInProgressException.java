package com.thentrees.gymhealthtech.exception;

public class WorkoutInProgressException extends BusinessException {
  public WorkoutInProgressException(String message) {
    super(message, "WORKOUT_IN_PROGRESS");
  }
}
