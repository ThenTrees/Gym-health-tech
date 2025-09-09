package com.thentrees.gymhealthtech.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExerciseImportRequest {
  @Valid
  @NotEmpty(message = "Exercise list cannot be empty")
  private List<CreateExerciseRequest> exercises;

  @JsonProperty("metadata")
  private ImportMetadata metadata;

  // Getters and setters
  public List<CreateExerciseRequest> getExercises() {
    return exercises;
  }

  public void setExercises(List<CreateExerciseRequest> exercises) {
    this.exercises = exercises;
  }

  public ImportMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ImportMetadata metadata) {
    this.metadata = metadata;
  }
}
