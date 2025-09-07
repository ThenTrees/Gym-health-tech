package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.ExerciseLevel;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercises")
public class Exercise extends BaseEntity {

  @Column(name = "slug", unique = true, nullable = false, length = 80)
  private String slug;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "level", nullable = false)
  private ExerciseLevel level;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "primary_muscle")
  private Muscle primaryMuscle;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "equipment")
  private EquipmentType equipment;

  @Column(name = "instructions", columnDefinition = "TEXT")
  private String instructions;

  @Column(name = "safety_notes", columnDefinition = "TEXT")
  private String safetyNotes;

  @Column(name = "thumbnail_url", columnDefinition = "TEXT")
  private String thumbnailUrl;

  @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
  private List<ExerciseMuscle> exerciseMuscles;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_type", referencedColumnName = "code", nullable = false)
  private ExerciseType exerciseType;
}
