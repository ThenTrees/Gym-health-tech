package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GeneratorMealPlanResponse {
  private boolean success;
  private MealData data;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class MealData {
    private String mealPlanId;
    private String planDate;
    private boolean isTrainingDay;
    private Meals meals;
    private ActualNutrition actualNutrition;
    private TargetNutrition targetNutrition;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meals {
      private List<FoodInfo> breakfast;
      private List<FoodInfo> lunch;
      private List<FoodInfo> dinner;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoodInfo {
      private UUID mealPlanItemId;
      private UUID mealTimeId;
      private double servings;
      private boolean completed;
      private int displayOrder;

      private Nutrition nutrition;
      private Food food;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nutrition {
      private double calories;
      private double protein;
      private double carbs;
      private double fat;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Food {
      private UUID id;
      private String name;
      private String nameVi;
      private String description;
      private String commonCombinations;

      private double calories;
      private double protein;
      private double carbs;
      private double fat;
      private double fiber;

      private String category;
      private String image;
      private String benefits;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActualNutrition {
      private double calories;
      private double protein;
      private double carbs;
      private double fats;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TargetNutrition {
      private double calories;
      private double protein;
      private double carbs;
      private double fats;
      private double caloriesForBreakfast;
      private double caloriesForLunch;
      private double caloriesForDinner;
    }
  }
}
