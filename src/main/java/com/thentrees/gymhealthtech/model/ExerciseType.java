package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercise_types")
public class ExerciseType {
  @Id
  @Column(name = "code", length = 32)
  private String code;

  @Column(name = "name", nullable = false, length = 64)
  private String name;

  @Column(name = "image_url")
  private String imageUrl;
}
