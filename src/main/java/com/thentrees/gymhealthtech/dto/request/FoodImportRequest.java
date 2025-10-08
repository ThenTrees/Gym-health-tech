package com.thentrees.gymhealthtech.dto.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FoodImportRequest {
  private String foodNameVi;
  private String foodName;
  private BigDecimal servingWeightGrams;
  private BigDecimal calories;
  private BigDecimal protein;
  private BigDecimal carbs;
  private BigDecimal fat;
  private String image;
  private String mealTime;
  private BigDecimal vitaminA;
  private BigDecimal vitaminC;
  private BigDecimal vitaminD;
  private String category;
  private BigDecimal fiber;
  private String description;
  private String detailedBenefits;
  private String commonCombinations;
  private String contraindications;
  private String alternativeFoods;
  private String searchTags;
}
