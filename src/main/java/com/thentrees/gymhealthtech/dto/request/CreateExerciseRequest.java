package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.enums.ExerciseLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class CreateExerciseRequest {
  @NotBlank(message = "Exercise name is required")
  @Size(max = 120, message = "Name must not exceed 120 characters")
  private String name;

  private ExerciseLevel exerciseLevel;

  private String equipmentTypeCode;

  @Size(max = 10000, message = "Instructions must not exceed 10000 characters")
  private List<String> instructions;

  @Size(max = 5000, message = "Safety notes must not exceed 5000 characters")
  private String safetyNotes;

  private String thumbnailUrl;

  // Muscles with roles
  @Valid private List<ExerciseMuscleRequest> muscles;

  @NotBlank(message = "Exercise category is required")
  private String exerciseCategory;

  private String exerciseType;

  private List<String> bodyParts;
}
