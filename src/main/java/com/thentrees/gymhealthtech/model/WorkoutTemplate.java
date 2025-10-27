package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.ObjectiveType;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workout_templates")
public class WorkoutTemplate extends BaseEntity {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "objective", nullable = false)
  private ObjectiveType objective;

  @Column(name = "duration_weeks", nullable = false)
  private Integer durationWeeks = 4;

  @Column(name = "sessions_per_week", nullable = false)
  private Integer sessionsPerWeek = 3;

  @Column(name = "thumbnail_url", length = 512)
  private String thumbnailUrl;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @OneToMany(mappedBy = "workoutTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TemplateDay> templateDays;
}
