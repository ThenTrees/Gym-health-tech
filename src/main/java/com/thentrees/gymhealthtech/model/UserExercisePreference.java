package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_exercise_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserExercisePreference extends BaseEntity{

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;


  // -1 .. 1
  @Column(name = "preference_score")
  private Double preferenceScore;


  // 1 .. 5
  @Column(name = "difficulty_rating")
  private Double difficultyRating;


  @Column(name = "notes", columnDefinition = "text")
  private String notes;
}
