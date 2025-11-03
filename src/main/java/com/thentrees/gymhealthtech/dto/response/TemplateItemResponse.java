package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateItemResponse {
  private UUID id;
  private Integer itemOrder;
  private Integer sets;
  private Integer reps;
  private Integer restSeconds;
  private String notes;

  // Thông tin bài tập (exercise)
  private UUID exerciseId;
  private String exerciseName;
  private String exerciseType;
  private String bodyPart;
  private String thumbnailUrl;
}
