package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.GeneratorWorkoutPlanRequest;
import com.thentrees.gymhealthtech.dto.response.GeneratorMealPlanResponse;
import com.thentrees.gymhealthtech.dto.response.GeneratorWorkoutPlanResponse;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j(topic = "AISERVICE")
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
  private final WebClient webClient;
  @Override
  public GeneratorWorkoutPlanResponse createGeneratorWorkoutPlan(Authentication authentication) {
    log.info("Creating workout plan via AI Service ...");
    User user = (User) authentication.getPrincipal();

    GeneratorWorkoutPlanRequest request = new GeneratorWorkoutPlanRequest();
    request.setUserId(user.getId());

    return webClient.post()
      .uri("/generate-plan")
      .bodyValue(request)
      .retrieve()
      .bodyToMono(GeneratorWorkoutPlanResponse.class)
      .block();
  }

  @Override
  public GeneratorMealPlanResponse createGeneratorMealPlan(Authentication authentication) {
    log.info("Creating meal plan via AI Service ...");
    User user = (User) authentication.getPrincipal();
    GeneratorWorkoutPlanRequest request = new GeneratorWorkoutPlanRequest();
    request.setUserId(user.getId());

    return webClient.post()
      .uri("/nutrition/meal-plan/generate")
      .bodyValue(request)
      .retrieve()
      .bodyToMono(GeneratorMealPlanResponse.class)
      .block();
  }
}
