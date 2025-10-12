package com.thentrees.gymhealthtech.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FoodResponse {
  private UUID id;
  private String foodName;
  private String foodNameVi;
  private String description;
  private BigDecimal servingWeightGrams; // in grams
  private BigDecimal calories; // in kcal
  private BigDecimal protein; // in grams
  private BigDecimal carbs;
  private BigDecimal fat; // in grams
  private BigDecimal fiber; // in grams

  private String category; // e.g., "Fruit", "Vegetable", "Dairy"
  private String imageUrl; // URL to food image
  private String mealTime; // e.g., "Breakfast", "Lunch", "Dinner", "Snack"
  private BigDecimal vitaminA; // in IU
  private BigDecimal vitaminC; // in mg
  private BigDecimal vitaminD; // in IU
  private String detailedBenefits; // Detailed health benefits of the food
  private String commonCombinations; // Common food combinations
  private String contraindications; // Any contraindications
  private String alternativeFoods; // Alternative food options
  private List<String> tags; // Comma-separated tags for search optimization
  private Boolean isActive; // Indicates if the food item is active
}
