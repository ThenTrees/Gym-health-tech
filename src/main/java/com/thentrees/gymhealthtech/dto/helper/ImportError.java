package com.thentrees.gymhealthtech.dto.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportError {
  private int lineNumber;
  private String exerciseName;
  private String field;
  private String errorMessage;
  private String errorType;

  public ImportError(int line, String name, String field, String message, String type) {
    this.lineNumber = line;
    this.exerciseName = name;
    this.field = field;
    this.errorMessage = message;
    this.errorType = type;
  }

  // Getters and setters
}
