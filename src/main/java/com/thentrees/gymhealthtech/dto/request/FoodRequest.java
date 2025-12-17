package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodRequest {
  @NotBlank(message = "Food name is required")
  private String foodName;

  @NotBlank(message = "Vietnamese food name is required")
  private String foodNameVi;

  private String description;
  @Min(value = 0, message = "Serving weight must be non-negative")
  private BigDecimal servingWeightGrams;

  @Min(value = 0, message = "Calories must be non-negative")
  private BigDecimal calories;

  @Min(value = 0, message = "Protein must be non-negative")
  private BigDecimal protein;

  @Min(value = 0, message = "Carbohydrates must be non-negative")
  private BigDecimal carbs;

  @Min(value = 0, message = "Fat must be non-negative")
  private BigDecimal fat;

  @Min(value = 0, message = "Fiber must be non-negative")
  private BigDecimal fiber;

  private BigDecimal vitaminA;

  private BigDecimal vitaminC;

  private BigDecimal vitaminD;

  @NotBlank(message = "Category is required")
  private String category;

  private String mealTime;

  private String imageUrl;

  private String detailedBenefits;

  private String commonCombinations;

  private String contraindications;

  private String alternativeFoods;

  private String tags;

}
