package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.custom.PremiumOnly;
import com.thentrees.gymhealthtech.dto.request.GeneratorWorkoutPlanRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.GeneratorMealPlanResponse;
import com.thentrees.gymhealthtech.dto.response.GeneratorWorkoutPlanResponse;
import com.thentrees.gymhealthtech.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1 + "/ai")
@RequiredArgsConstructor

public class AIController {

  private final AIService aiService;

  @PremiumOnly
  @PostMapping("/generate-plan")
  public ResponseEntity<APIResponse<GeneratorWorkoutPlanResponse>> generateWorkoutPlan(@RequestBody GeneratorWorkoutPlanRequest request) {
    GeneratorWorkoutPlanResponse planResponse = aiService.createGeneratorWorkoutPlan(request);
    return ResponseEntity.ok(APIResponse.success(planResponse));
  }

  @PostMapping("/meal-plan/generate")
  public ResponseEntity<GeneratorMealPlanResponse> generationMealPlan(Authentication authentication) {
    GeneratorMealPlanResponse planResponse = aiService.createGeneratorMealPlan(authentication);
    return ResponseEntity.ok(planResponse);
  }
}
