package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_plans")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MealPlan extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "plan_date", nullable = false)
  private LocalDate planDate;

  @Column(name = "total_calories")
  private Integer totalCalories;

  @Column(name = "total_protein")
  private BigDecimal totalProtein;

  @Column(name = "total_carbs")
  private BigDecimal totalCarbs;

  @Column(name = "total_fat")
  private BigDecimal totalFat;

  @Column(name = "is_training_day")
  private Boolean isTrainingDay = false;

  @Column(name = "base_calories")
  private Integer baseCalories;

  @Column(name = "workout_adjustment")
  private Integer workoutAdjustment = 0;

  @Column(name = "ai_reasoning", columnDefinition = "TEXT")
  private String aiReasoning;

  @Column(name = "ai_tips", columnDefinition = "jsonb")
  private List<String> aiTips;

  private String status = "generated";

  @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL)
  private List<MealPlanItem> items = new ArrayList<>();
}
