package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.AIServiceWorkoutPlanResponse;
import com.thentrees.gymhealthtech.dto.response.GeneratorWorkoutPlanResponse;
import com.thentrees.gymhealthtech.mapper.common.PrescriptionMapper;
import com.thentrees.gymhealthtech.model.Exercise;
import com.thentrees.gymhealthtech.model.PlanItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting AI Service responses to internal DTOs and entities.
 * Handles the complex mapping from AIServiceWorkoutPlanResponse to GeneratorWorkoutPlanResponse
 * and from AI response data to PlanItem entities.
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "AI-SERVICE-MAPPER")
public class AIServiceMapper {

  private final ObjectMapper objectMapper;
  private final PrescriptionMapper prescriptionMapper;

  /**
   * Map từ AIServiceWorkoutPlanResponse sang GeneratorWorkoutPlanResponse
   * Convert: success (boolean) -> status (string), data -> metadata
   */
  public GeneratorWorkoutPlanResponse toGeneratorResponse(AIServiceWorkoutPlanResponse aiResponse) {
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
      GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan workoutPlan = toWorkoutPlan(data.getWorkoutPlan());
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
  public GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan toWorkoutPlan(
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
        GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay planDay = toPlanDay(aiPlanDay);
        planDays.add(planDay);
      }
      workoutPlan.setPlanDays(planDays);
    }

    return workoutPlan;
  }

  /**
   * Map PlanDay từ AI service response
   */
  public GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay toPlanDay(
      AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData aiPlanDay) {

    GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay planDay =
        new GeneratorWorkoutPlanResponse.Metadata.WorkoutPlan.PlanDay();

    planDay.setDayIndex(aiPlanDay.getDayIndex());
    planDay.setSplitName(aiPlanDay.getSplitName());
    planDay.setScheduledDate(aiPlanDay.getScheduledDate()); // Keep as String in nested class

    // Map planItems: Convert từ PlanItemData sang PlanItem model
    if (aiPlanDay.getPlanItems() != null) {
      List<PlanItem> planItems = new ArrayList<>();
      for (AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData.PlanItemData aiPlanItem : aiPlanDay.getPlanItems()) {
        PlanItem planItem = toPlanItem(aiPlanItem);
        planItems.add(planItem);
      }
      planDay.setPlanItems(planItems);
    }

    return planDay;
  }

  /**
   * Map PlanItem từ AI service response
   */
  public PlanItem toPlanItem(
      AIServiceWorkoutPlanResponse.WorkoutPlanData.WorkoutPlan.PlanDayData.PlanItemData aiPlanItem) {

    PlanItem planItem = new PlanItem();
    planItem.setItemIndex(aiPlanItem.getItemIndex());
    planItem.setNotes(aiPlanItem.getNotes());

    // Convert prescription Object to JsonNode using PrescriptionMapper
    if (aiPlanItem.getPrescription() != null) {
      planItem.setPrescription(prescriptionMapper.objectToJsonNode(aiPlanItem.getPrescription()));
    }

    // Map Exercise - convert từ ExerciseData sang Exercise model
    if (aiPlanItem.getExercise() != null) {
      try {
        // Convert ExerciseData object to Exercise model using ObjectMapper
        // ExerciseData structure matches Exercise model structure
        JsonNode exerciseNode = objectMapper.valueToTree(aiPlanItem.getExercise());
        Exercise exercise = objectMapper.treeToValue(exerciseNode, Exercise.class);
        planItem.setExercise(exercise);
      } catch (Exception e) {
        log.warn("Failed to map exercise from AI service response", e);
      }
    }

    return planItem;
  }
}

