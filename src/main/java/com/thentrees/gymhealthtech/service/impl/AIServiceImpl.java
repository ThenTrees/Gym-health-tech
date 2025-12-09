package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.thentrees.gymhealthtech.dto.request.GeneratorWorkoutPlanRequest;
import com.thentrees.gymhealthtech.dto.response.AIServiceWorkoutPlanResponse;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "AI-SERVICE")
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
  private final WebClient webClient;
  private final ObjectMapper mapper;
  @Override
  public GeneratorWorkoutPlanResponse createGeneratorWorkoutPlan(GeneratorWorkoutPlanRequest genPlanRequest) {
    log.info("Creating workout plan via AI Service ...");

    return webClient.post()
      .uri("/generate-plan")
      .bodyValue(genPlanRequest)
      .retrieve()
      .bodyToMono(String.class)   // STEP 1: đọc raw response
      .flatMap(raw -> {
        try {
          // Parse JSON vào wrapper class khớp với cấu trúc AI service
          AIServiceWorkoutPlanResponse aiResponse = mapper.readValue(raw, AIServiceWorkoutPlanResponse.class);

          // Map từ AIServiceWorkoutPlanResponse sang GeneratorWorkoutPlanResponse
          GeneratorWorkoutPlanResponse response = mapToGeneratorResponse(aiResponse);
          return Mono.just(response);
        } catch (Exception e) {
          log.error("Cannot parse AI response", e);
          return Mono.error(e);
        }
      })
      .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
        .filter(ex -> ex instanceof WebClientRequestException))
      .onErrorResume(ex -> {
        log.error("AI Service fallback triggered due to: {}", ex.getMessage());
        return Mono.just(
          GeneratorWorkoutPlanResponse.builder()
            .status("error")
            .message("Hiện tại AI Service đang tạm ngưng. Vui lòng thử lại sau.")
            .build()
        );
      })
      .block();
  }

  /**
   * Map từ AIServiceWorkoutPlanResponse sang GeneratorWorkoutPlanResponse
   * Convert: success (boolean) -> status (string), data -> metadata
   */
  private GeneratorWorkoutPlanResponse mapToGeneratorResponse(AIServiceWorkoutPlanResponse aiResponse) {
    if (aiResponse == null || aiResponse.getData() == null) {
      return GeneratorWorkoutPlanResponse.builder()
        .status("error")
        .message(aiResponse != null ? aiResponse.getMessage() : "Invalid response from AI Service")
        .build();
    }

    AIServiceWorkoutPlanResponse.WorkoutPlanData data = aiResponse.getData();

    // Convert success boolean to status string
    String status = (aiResponse.getSuccess() != null && aiResponse.getSuccess()) ? "success" : "error";

    // Build Metadata
    GeneratorWorkoutPlanResponse.Metadata metadata = new GeneratorWorkoutPlanResponse.Metadata();
    metadata.setStartTime(data.getStartTime() != null ? String.valueOf(data.getStartTime()) : null);
    metadata.setGenerationTime(data.getGenerationTime() != null ? String.valueOf(data.getGenerationTime()) : null);
    metadata.setTotalExercises(data.getTotalExercises());
    metadata.setAvgSessionDuration(data.getAvgSessionDuration());

    // Map WorkoutPlan
    if (data.getWorkoutPlan() != null) {
      GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan workoutPlan = mapWorkoutPlan(data.getWorkoutPlan());
      metadata.setWorkoutPlan(workoutPlan);
    }

    // Build final response
    return GeneratorWorkoutPlanResponse.builder()
      .status(status)
      .message(aiResponse.getMessage())
      .metadata(metadata)
      .build();
  }

  /**
   * Map WorkoutPlan từ AI service response
   */
  private GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan mapWorkoutPlan(
      AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan aiWorkoutPlan) {

    GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan workoutPlan =
        new GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan();

    workoutPlan.setId(aiWorkoutPlan.getId());
    workoutPlan.setUserId(aiWorkoutPlan.getUserId());
    workoutPlan.setGoalId(aiWorkoutPlan.getGoalId());
    workoutPlan.setTitle(aiWorkoutPlan.getTitle());
    workoutPlan.setDescription(aiWorkoutPlan.getDescription());
    workoutPlan.setTotalWeeks(aiWorkoutPlan.getTotalWeeks());
    workoutPlan.setTotalDays(aiWorkoutPlan.getTotalDays());
    workoutPlan.setCreatedAt(aiWorkoutPlan.getCreatedAt());
    workoutPlan.setEndDate(aiWorkoutPlan.getEndDate());

    // Map planDays: Convert từ PlanDayData sang nested PlanDay class
    if (aiWorkoutPlan.getPlanDays() != null) {
      List<GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay> planDays = new ArrayList<>();
      for (AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData aiPlanDay : aiWorkoutPlan.getPlanDays()) {
        GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay planDay = mapPlanDay(aiPlanDay);
        planDays.add(planDay);
      }
      workoutPlan.setPlanDays(planDays);
    }

    return workoutPlan;
  }

  /**
   * Map PlanDay từ AI service response
   */
  private GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay mapPlanDay(
      AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData aiPlanDay) {

    GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay planDay =
        new GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay();

    planDay.setDayIndex(aiPlanDay.getDayIndex());
    planDay.setSplitName(aiPlanDay.getSplitName());
    planDay.setScheduledDate(aiPlanDay.getScheduledDate()); // Keep as String in nested class

    // Map planItems: Convert từ PlanItemData sang PlanItem model
    if (aiPlanDay.getPlanItems() != null) {
      List<com.thentrees.gymhealthtech.model.PlanItem> planItems = new ArrayList<>();
      for (AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData.PlanItemData aiPlanItem : aiPlanDay.getPlanItems()) {
        com.thentrees.gymhealthtech.model.PlanItem planItem = mapPlanItem(aiPlanItem);
        planItems.add(planItem);
      }
      planDay.setPlanItems(planItems);
    }

    return planDay;
  }

  /**
   * Map PlanItem từ AI service response
   */
  private com.thentrees.gymhealthtech.model.PlanItem mapPlanItem(
      AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData.PlanItemData aiPlanItem) {

    com.thentrees.gymhealthtech.model.PlanItem planItem = new com.thentrees.gymhealthtech.model.PlanItem();
    planItem.setItemIndex(aiPlanItem.getItemIndex());
    planItem.setNotes(aiPlanItem.getNotes());

    // Convert prescription Object to JsonNode
    if (aiPlanItem.getPrescription() != null) {
      try {
        JsonNode prescriptionNode = mapper.valueToTree(aiPlanItem.getPrescription());
        planItem.setPrescription(prescriptionNode);
      } catch (Exception e) {
        log.warn("Failed to convert prescription to JsonNode", e);
        // Create empty prescription node
        planItem.setPrescription(mapper.createObjectNode());
      }
    }

    // Map Exercise - cần convert từ ExerciseData sang Exercise model
    if (aiPlanItem.getExercise() != null) {
      try {
        // Convert ExerciseData object to Exercise model using ObjectMapper
        // ExerciseData structure matches Exercise model structure
        JsonNode exerciseNode = mapper.valueToTree(aiPlanItem.getExercise());
        com.thentrees.gymhealthtech.model.Exercise exercise =
            mapper.treeToValue(exerciseNode, com.thentrees.gymhealthtech.model.Exercise.class);
        planItem.setExercise(exercise);
      } catch (Exception e) {
        log.warn("Failed to map exercise", e);
      }
    }

    return planItem;
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
