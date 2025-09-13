package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercise_categories")
public class ExerciseCategory {
  @Id
  @Column(name = "code", length = 32)
  private String code;

  @Column(name = "name", nullable = false, length = 64)
  private String name;

  @Column(name = "image_url")
  private String imageUrl;

  @OneToMany(mappedBy = "exerciseCategory", cascade = CascadeType.ALL, orphanRemoval = false)
  private Set<Exercise> exercises = new HashSet<>();
}
