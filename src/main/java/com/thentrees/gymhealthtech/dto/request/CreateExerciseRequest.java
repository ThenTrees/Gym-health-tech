package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
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
  @NotBlank(message = ValidationMessages.EXERCISE_NAME_REQUIRE)
  @Size(max = 120, message = ValidationMessages.EXERCISE_NAME_EXCEED_120)
  private String name;

  private ExerciseLevel exerciseLevel;

  private String equipmentTypeCode;

  @Size(max = 10000, message = ValidationMessages.INTRODUCE_EXCEED_10000)
  private List<String> instructions;

  @Size(max = 5000, message = ValidationMessages.SAFETY_NOTE_EXCEED_5000)
  private String safetyNotes;

  private String thumbnailUrl;

  // Muscles with roles
  @Valid private List<ExerciseMuscleRequest> muscles;

  @NotBlank(message = ValidationMessages.EXERCISE_CATEGORY_REQUIRE)
  private String exerciseCategory;

  private String exerciseType;

  private List<String> bodyParts;
}
