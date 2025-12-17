package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_plan_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MealPlanItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal_plan_id", nullable = false)
  private MealPlan mealPlan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal_time_id", nullable = false)
  private MealTime mealTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "food_id")
  private Food food;

  @Column(name = "food_name", nullable = false)
  private String foodName;

  @Column(nullable = false)
  private BigDecimal servings;

  @Column(nullable = false)
  private BigDecimal calories;

  private BigDecimal protein;
  private BigDecimal carbs;
  private BigDecimal fat;

  @Column(name = "is_completed")
  private Boolean isCompleted = false;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "display_order")
  private Integer displayOrder = 0;
}
