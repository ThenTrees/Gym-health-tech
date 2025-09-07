package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.common.ExerciseLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  //  @NotBlank(message = "Slug is required")
  //  @Size(max = 80, message = "Slug must not exceed 80 characters")
  //  private String slug;

  @NotNull(message = "Level is required")
  private ExerciseLevel level;

  @NotBlank(message = "Primary muscle is required")
  private String primaryMuscleCode;

  private String equipmentTypeCode;

  @Size(max = 10000, message = "Instructions must not exceed 10000 characters")
  private String instructions;

  @Size(max = 5000, message = "Safety notes must not exceed 5000 characters")
  private String safetyNotes;

  private String thumbnailUrl;

  // Muscles with roles
  @Valid private List<ExerciseMuscleRequest> muscles;

  @NotBlank(message = "Exercise type is required")
  private String exerciseType;
}
