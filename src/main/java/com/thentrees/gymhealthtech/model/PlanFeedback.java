package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeedback extends BaseEntity{

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "overall_rating")
  private Double overallRating;

  @Column(name = "difficulty_rating")
  private Double difficultyRating;

  @Column(name = "enjoyment_rating")
  private Double enjoymentRating;

  @Column(name = "feedback_text", columnDefinition = "text")
  private String feedbackText;

}
