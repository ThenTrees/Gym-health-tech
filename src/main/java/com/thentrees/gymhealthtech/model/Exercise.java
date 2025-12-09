package com.thentrees.gymhealthtech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thentrees.gymhealthtech.enums.ExerciseType;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercises")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Exercise extends BaseEntity {

  @Column(name = "slug", unique = true, nullable = false, length = 80)
  private String slug;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "difficulty_level")
  private int difficultyLevel;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "primary_muscle")
  private Muscle primaryMuscle;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "equipment")
  private Equipment equipment;

  @Column(name = "instructions", columnDefinition = "TEXT")
  private String instructions;

  @Column(name = "safety_notes", columnDefinition = "TEXT")
  private String safetyNotes;

  @Column(name = "thumbnail_url", columnDefinition = "TEXT")
  private String thumbnailUrl;

  @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
  private List<ExerciseMuscle> exerciseMuscles;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "exercise_category", referencedColumnName = "code", nullable = false)
  private ExerciseCategory exerciseCategory;

  @Column(name = "body_part", length = 100)
  private String bodyPart;

  @Enumerated(EnumType.STRING)
  @Column(name = "exercise_type")
  private ExerciseType exerciseType;
}
