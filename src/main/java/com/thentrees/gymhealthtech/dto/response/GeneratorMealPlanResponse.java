package com.thentrees.gymhealthtech.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratorMealPlanResponse {
  private boolean success;
  private String message;
  private MealData data;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class MealData {
    private String mealPlanId;
    private String planDate;
    private boolean isTrainingDay;
    private Meals meals;
    private ActualNutrition actualNutrition;
    private TargetNutrition targetNutrition;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meals {
      private List<FoodInfo> breakfast;
      private List<FoodInfo> lunch;
      private List<FoodInfo> dinner;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoodInfo {
      private String mealPlanItemId;
      private String mealTimeId;
      private String servings;
      private boolean completed;
      private int displayOrder;
      private Nutrition nutrition;
      private Food food;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nutrition {
      private String calories;
      private String protein;
      private String carbs;
      private String fat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Food {
      private String id;
      private String name;
      private String nameVi;
      private String description;
      private String commonCombinations;

      private String calories;
      private String protein;
      private String carbs;
      private String fat;
      private String fiber;

      private String category;
      private String image;
      private String benefits;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActualNutrition {
      private String calories;
      private String protein;
      private String carbs;
      private String fats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TargetNutrition {
      private String calories;
      private String protein;
      private String carbs;
      private String fats;

      private String caloriesForBreakfast;
      private String caloriesForLunch;
      private String caloriesForDinner;
    }
  }
}
