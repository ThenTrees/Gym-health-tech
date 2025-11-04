package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.ObjectiveType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "workout_templates")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutTemplate extends BaseEntity {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "objective", nullable = false)
  private ObjectiveType objective;

  @Column(name = "duration_weeks", nullable = false)
  private Integer durationWeeks;

  @Column(name = "sessions_per_week", nullable = false)
  private Integer sessionsPerWeek;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "total_used", nullable = false)
  @Builder.Default
  private Integer totalUsed = 0;

  @OneToMany(mappedBy = "workoutTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<TemplateDay> templateDays = new ArrayList<>();
}
