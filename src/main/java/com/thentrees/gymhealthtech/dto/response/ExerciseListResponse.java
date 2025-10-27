package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.enums.ExerciseLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseListResponse {
  private UUID id;
  private String slug;
  private String name;
  private ExerciseLevel level;
  private List<String> primaryMuscle;
  private String equipment;
  private String thumbnailUrl;
  private List<String> instructions;
  private String safetyNotes;
  private String exerciseCategory;
  private String exerciseType;
  private String bodyPart;
  // Additional info
  private List<String> secondaryMuscles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
