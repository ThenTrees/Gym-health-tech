package com.thentrees.gymhealthtech.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExerciseImportDto {

  @JsonProperty("name")
  @NotBlank(message = "Exercise name is required")
  private String name;

  @JsonProperty("category")
  private String category;

  @JsonProperty("bodyPart")
  private String bodyPart;

  @JsonProperty("equipment")
  private String equipment;

  @JsonProperty("target")
  private String targetMuscle;

  @JsonProperty("secondaryMuscles")
  private List<String> secondaryMuscles = new ArrayList<>();

  @JsonProperty("instructions")
  private List<String> instructions = new ArrayList<>();

  @JsonProperty("gifUrl")
  @URL(message = "GIF URL must be valid")
  private String gifUrl;

  @JsonProperty("difficulty")
  private String difficulty;

  @JsonProperty("type")
  private String type;
}
