package com.thentrees.gymhealthtech.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foods")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Food extends BaseEntity {

  @Column(name = "food_name", nullable = false)
  private String foodName;

  @Column(name = "food_name_vi")
  private String foodNameVi;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "serving_weight_grams", nullable = false)
  private BigDecimal servingWeightGrams;

  @Column(nullable = false)
  private BigDecimal calories;

  private BigDecimal protein;
  private BigDecimal carbs;
  private BigDecimal fat;
  private BigDecimal fiber;

  @Column(name = "vitamin_a")
  private BigDecimal vitaminA;

  @Column(name = "vitamin_c")
  private BigDecimal vitaminC;

  @Column(name = "vitamin_d")
  private BigDecimal vitaminD;

  @Column(nullable = false)
  private String category;

  @Column(name = "meal_time", nullable = false)
  private String mealTime;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "detailed_benefits")
  private String detailedBenefits;

  @Column(name = "common_combinations")
  private String commonCombinations;

  @Column(name = "contraindications")
  private String contraindications;

  @Column(name = "alternative_foods")
  private String alternativeFoods;

  @Column(columnDefinition = "text[]")
  private List<String> tags;

  @Column(name = "is_active")
  private Boolean isActive = true;
}
