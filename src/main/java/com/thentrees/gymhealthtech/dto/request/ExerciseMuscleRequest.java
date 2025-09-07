package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class ExerciseMuscleRequest {
  @NotBlank(message = "Muscle code is required")
  private String muscleCode;

  @NotBlank(message = "Role is required")
  @Pattern(regexp = "PRIMARY|SECONDARY", message = "Role must be 'primary' or 'secondary'")
  private String role;
}
