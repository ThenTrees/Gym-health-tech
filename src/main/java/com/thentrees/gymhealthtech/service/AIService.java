package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.response.GeneratorMealPlanResponse;
import com.thentrees.gymhealthtech.dto.response.GeneratorWorkoutPlanResponse;
import org.springframework.security.core.Authentication;

public interface AIService {
  GeneratorWorkoutPlanResponse createGeneratorWorkoutPlan(Authentication authentication);
  GeneratorMealPlanResponse createGeneratorMealPlan(Authentication authentication);

}
