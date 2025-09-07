package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseMuscleResponse {
  private String muscleCode;
  private String muscleName;
  private String role;
}
