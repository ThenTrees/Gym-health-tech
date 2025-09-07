package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.ExerciseLevel;
import jakarta.persistence.*;
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
public class ExerciseDetailResponse {
  private UUID id;
  private String slug;
  private String name;
  private ExerciseLevel level;
  private MuscleResponse primaryMuscle;
  private EquipmentTypeResponse equipment;
  private String instructions;
  private String safetyNotes;
  private String thumbnailUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  // Relations
  private List<ExerciseMuscleResponse> muscles;
  private List<ExerciseAssetResponse> assets;
}
