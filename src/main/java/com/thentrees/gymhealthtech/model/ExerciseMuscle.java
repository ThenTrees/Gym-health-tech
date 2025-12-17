package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercise_muscles")
public class ExerciseMuscle {

  @EmbeddedId private ExerciseMuscleId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("exerciseId")
  @JoinColumn(name = "exercise_id")
  private Exercise exercise;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("muscleCode")
  @JoinColumn(name = "muscle_code")
  private Muscle muscle;

  @Column(name = "role", nullable = false, length = 16)
  private String role; // 'primary' or 'secondary', phan biet nhom co vai tro chinh hay phu
}
