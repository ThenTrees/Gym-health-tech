package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.common.ExerciseLevel;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSearchRequest implements Serializable {
  private String keyword;
  private ExerciseLevel level;
  private String primaryMuscle;
  private List<String> musclesCodes;
  private String equipmentType;
  private String exerciseType;

  // Pagination
  private Integer page = 0;
  private Integer size = 20;
  private String sortBy = "name";
  private String sortDirection = "ASC";
}
