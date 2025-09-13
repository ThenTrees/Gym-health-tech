package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.model.Exercise;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseCategoryResponse {
  private String code;
  private String name;
  private String imageUrl;
  private Set<Exercise> exercises = new HashSet<>();
}
