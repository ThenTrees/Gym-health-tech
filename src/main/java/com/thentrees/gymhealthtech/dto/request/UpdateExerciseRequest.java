package com.thentrees.gymhealthtech.dto.request;

import lombok.Data;

@Data
public class UpdateExerciseRequest {
  private String name;
  private String instructions;
  private String safetyNotes;
  private String bodyPart;
  private String exerciseType;
}
