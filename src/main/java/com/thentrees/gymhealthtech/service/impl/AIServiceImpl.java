package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.constant.ErrorCodes;
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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j(topic = "AI-SERVICE")
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
      .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Retry up to 3 times with exponential backoff -> 2 -> 8 -> 16
        .filter(throwable -> throwable instanceof WebClientRequestException)) // Retry only for network-related exceptions
      .onErrorResume(ex -> {
        log.error("AI Service fallback triggered due to: {}",ex.getMessage());
        return Mono.just(
          GeneratorWorkoutPlanResponse.builder()
            .status("error")
            .message("Hiện tại AI Service đang tạm ngưng. Vui lòng thử lại sau.")
            .build()
        );
      })
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
      .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Retry up to 3 times with exponential backoff
        .filter(throwable -> throwable instanceof WebClientRequestException)) // Retry only for network-related exceptions
      .onErrorResume(ex -> {
        log.error("AI Service fallback triggered due to: {}",ex.getMessage());
        return Mono.just(
          GeneratorMealPlanResponse.builder()
            .success(false)
            .message("Hiện tại AI Service đang tạm ngưng. Vui lòng thử lại sau.")
            .build()
        );
      })
      .block();
  }
}
