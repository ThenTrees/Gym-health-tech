package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.custom.PremiumOnly;
import com.thentrees.gymhealthtech.dto.request.ChatRequest;
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
@PremiumOnly
public class AIController {

  private final AIService aiService;

  @PostMapping("/generate-plan")
  public ResponseEntity<APIResponse<GeneratorWorkoutPlanResponse>> generateWorkoutPlan(Authentication authentication) {
    GeneratorWorkoutPlanResponse planResponse = aiService.createGeneratorWorkoutPlan(authentication);
    return ResponseEntity.ok(APIResponse.success(planResponse));
  }

  @PostMapping("/chatbot/chat")
  public ResponseEntity<APIResponse<GeneratorWorkoutPlanResponse>> chatBot(@RequestBody ChatRequest request, Authentication authentication) {
    GeneratorWorkoutPlanResponse planResponse = aiService.createGeneratorWorkoutPlan(authentication);
    return ResponseEntity.ok(APIResponse.success(planResponse));
  }

  @PostMapping("/meal-plan/generate")
  public ResponseEntity<GeneratorMealPlanResponse> generationMealPlan(Authentication authentication) {
    GeneratorMealPlanResponse planResponse = aiService.createGeneratorMealPlan(authentication);
    return ResponseEntity.ok(planResponse);
  }
}
