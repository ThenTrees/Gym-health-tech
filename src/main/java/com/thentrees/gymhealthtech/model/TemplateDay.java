package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "template_days")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateDay extends BaseEntity{

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private WorkoutTemplate workoutTemplate;

  @Column(name = "day_name", nullable = false, length = 100)
  private String dayName; // "Day 1: Upper Body", "Ngày 1: Tập tay"

  @Column(name = "day_order", nullable = false)
  private Integer dayOrder; // 1, 2, 3, 4...

  @Column(name = "day_of_week")
  private Integer dayOfWeek; // 1=Monday, 2=Tuesday... (optional)

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @OneToMany(mappedBy = "templateDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TemplateItem> templateItems;

  // Computed field for API response
  @Transient private Integer totalExercises;
}
