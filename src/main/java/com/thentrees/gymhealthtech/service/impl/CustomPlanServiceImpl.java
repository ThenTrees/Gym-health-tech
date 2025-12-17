package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.exception.ValidationException;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.mapper.ExerciseMapper;
import com.thentrees.gymhealthtech.mapper.PlanMapper;
import com.thentrees.gymhealthtech.mapper.common.PrescriptionMapper;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.service.CustomPlanService;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPlanServiceImpl implements CustomPlanService {
  private final PlanRepository planRepository;
  private final PlanDayRepository planDayRepository;
  private final PlanItemRepository planItemRepository;
  private final ExerciseRepository exerciseRepository;
  private final SessionRepository sessionRepository;
  private final PlanMapper planMapper;
  private final ExerciseMapper exerciseMapper;
  private final PrescriptionMapper prescriptionMapper;
  private final GoalRepository goalRepository;


  @Override
  @Transactional(readOnly = true)
  public PagedResponse<PlanSummaryResponse> getAllPlansForUser(
    Authentication authentication, PlanSearchRequest searchCriteria, Pageable pageable) {

    User user = (User) authentication.getPrincipal();

    // This would use a custom repository method with dynamic query building
    Page<Plan> plans = planRepository.findPlansWithCriteria(user.getId(), searchCriteria, pageable);
    Page<PlanSummaryResponse> planResponses = plans.map(plan -> planMapper.toSummaryResponse(plan));
    return PagedResponse.of(planResponses);
  }

  @Transactional
  @Override
  public PlanResponse createCustomPlan(Authentication authentication, CreateCustomPlanRequest request) {

    User user = (User) authentication.getPrincipal();

    // get all exercise IDs from the request
    Set<UUID> exerciseIds =
        request.getPlanDays().stream()
            .flatMap(day -> day.getPlanItems().stream())
            .map(CreateCustomPlanItemRequest::getExerciseId)
            .collect(Collectors.toSet());

    // Validate exercises
    Map<UUID, Exercise> exerciseMap =
        exerciseRepository.findAllById(exerciseIds).stream()
            .collect(Collectors.toMap(Exercise::getId, e -> e));

    if (exerciseMap.size() != exerciseIds.size()) {
      throw new ValidationException("Some exercises not found");
    }
    Goal goal = null;
    LocalDate currentDate = LocalDate.now();
    if (request.getGoalId() != null) {
      goal = goalRepository.findById(request.getGoalId()).orElse(null);
    }

    // Create the plan
    Plan plan = new Plan();
    plan.setUser(user);
    plan.setGoal(goal);
    plan.setTitle(request.getTitle());
    plan.setSource(PlanSourceType.CUSTOM);
    plan.setCycleWeeks(request.getCycleWeeks());
    plan.setStatus(PlanStatusType.DRAFT); // New plans are DRAFT by default
    plan.setEndDate(currentDate.plusWeeks(request.getCycleWeeks()));
    plan.setNotes(request.getNotes());
    plan = planRepository.save(plan);

    // Create plan days
    List<PlanDay> planDays = new ArrayList<>();
    for (CreateCustomPlanDayRequest dayRequest : request.getPlanDays()) {
      PlanDay planDay = new PlanDay();
      planDay.setPlan(plan);
      planDay.setDayIndex(dayRequest.getDayIndex());
      planDay.setSplitName(dayRequest.getSplitName());
      planDay.setScheduledDate(dayRequest.getScheduledDate());

      planDay = planDayRepository.save(planDay);

      // Create plan items
      List<PlanItem> planItems = new ArrayList<>();
      for (CreateCustomPlanItemRequest itemRequest : dayRequest.getPlanItems()) {
        Exercise exercise = exerciseMap.get(itemRequest.getExerciseId());

        PlanItem planItem = new PlanItem();
        planItem.setPlanDay(planDay);
        planItem.setExercise(exercise);
        planItem.setItemIndex(itemRequest.getItemIndex());
        planItem.setPrescription(prescriptionMapper.toJsonNode(itemRequest.getPrescription()));
        planItem.setNotes(itemRequest.getNotes());

        planItems.add(planItemRepository.save(planItem));
      }

      planDay.setPlanItems(planItems);
      planDays.add(planDay);
    }
    plan.setPlanDays(planDays);
    int completedSessions = sessionRepository.countCompletedSessionsByPlanId(plan.getId());
    return planMapper.toResponseWithDetails(plan, completedSessions, true);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanResponse> getUserPlans(Authentication authentication) {

    User user = (User) authentication.getPrincipal();
    List<Plan> plans = planRepository.findByUserId(user.getId());
    return plans.stream()
        .map(plan -> {
          int completedSessions = sessionRepository.countCompletedSessionsByPlanId(plan.getId());
          return planMapper.toResponseWithDetails(plan, completedSessions, true);
        })
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PlanResponse getPlanDetails(UUID planId) {
    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));
    int completedSessions = sessionRepository.countCompletedSessionsByPlanId(plan.getId());
    return planMapper.toResponseWithDetails(plan, completedSessions, true);
  }

  @Transactional
  @Override
  public PlanResponse updatePlan(Authentication authentication, UUID planId, UpdateCustomPlanRequest request) {
    User user = (User) authentication.getPrincipal();
    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));

    // Update basic fields
    if (request.getTitle() != null) {
      plan.setTitle(request.getTitle());
    }

    if (request.getCycleWeeks() != null) {
      plan.setCycleWeeks(request.getCycleWeeks());
    }

    if (request.getStatus() != null) {
      if (request.getStatus().equalsIgnoreCase(PlanStatusType.ACTIVE.toString())) {
        List<Plan> listPlan = planRepository.findByUserId(user.getId());
        for (Plan planItem : listPlan) {
          planItem.setStatus(PlanStatusType.PAUSE);
          planRepository.save(planItem);
        }
      }
      plan.setStatus(PlanStatusType.valueOf(request.getStatus()));
    }

    plan = planRepository.save(plan);

    log.info("Successfully updated plan: {}", planId);
    int completedSessions = sessionRepository.countCompletedSessionsByPlanId(plan.getId());
    return planMapper.toResponseWithDetails(plan, completedSessions, true);
  }

  @Transactional
  @Override
  public void deletePlan(Authentication authentication, UUID planId) {
    User user = (User) authentication.getPrincipal();
    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));

    // check must your plan
    if (!plan.getUser().getId().equals(user.getId())) {
      throw new BusinessException("You can del your plan!");
    }

    // Check if there are any completed sessions
    boolean hasCompletedSessions =
        sessionRepository.existsByPlanDayPlanIdAndStatus(planId, SessionStatus.COMPLETED);

    if (hasCompletedSessions) {
      // Soft delete - mark as inactive instead of hard delete
      plan.setStatus(PlanStatusType.ARCHIVED);
      planRepository.save(plan);
      log.info("Soft deleted plan with completed sessions: {}", planId);
    } else {
      // Hard delete if no sessions
      planRepository.delete(plan);
      log.info("Hard deleted plan: {}", planId);
    }
  }

  @Transactional
  @Override
  public PlanDayResponse addDayToPlan(
      Authentication authentication, UUID planId, CreateCustomPlanDayRequest request) {

    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

    // Validate exercises
    Set<UUID> exerciseIds =
        request.getPlanItems().stream()
            .map(CreateCustomPlanItemRequest::getExerciseId)
            .collect(Collectors.toSet());

    Map<UUID, Exercise> exerciseMap =
        exerciseRepository.findAllById(exerciseIds).stream()
            .collect(Collectors.toMap(Exercise::getId, e -> e));

    if (exerciseMap.size() != exerciseIds.size()) {
      throw new ValidationException("Some exercises not found");
    }

    // Create plan day
    PlanDay planDay = new PlanDay();
    planDay.setPlan(plan);
    planDay.setDayIndex(request.getDayIndex());
    planDay.setSplitName(request.getSplitName());
    planDay.setScheduledDate(request.getScheduledDate());

    planDay = planDayRepository.save(planDay);

    // Create plan items
    List<PlanItem> planItems = new ArrayList<>();
    for (CreateCustomPlanItemRequest itemRequest : request.getPlanItems()) {
      Exercise exercise = exerciseMap.get(itemRequest.getExerciseId());

      PlanItem planItem = new PlanItem();
      planItem.setPlanDay(planDay);
      planItem.setExercise(exercise);
      planItem.setItemIndex(itemRequest.getItemIndex());
      planItem.setPrescription(prescriptionMapper.toJsonNode(itemRequest.getPrescription()));
      planItem.setNotes(itemRequest.getNotes());

      planItems.add(planItemRepository.save(planItem));
    }

    planDay.setPlanItems(planItems);

    log.info("Successfully added day to plan: {}", planId);
    int estimatedMinutes = planDay.getPlanItems() != null
        ? planDay.getPlanItems().stream().mapToInt(this::estimateExerciseDuration).sum()
        : 0;
    boolean isCompleted = sessionRepository.existsByPlanDayIdAndStatus(planDay.getId(), SessionStatus.COMPLETED);
    return planMapper.toPlanDayResponse(planDay, true, estimatedMinutes, isCompleted);
  }

  @Transactional
  @Override
  public void removeDayFromPlan(Authentication authentication, UUID planId, UUID planDayId) {

    User user = (User) authentication.getPrincipal();

    Plan plan = planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan not found"));


    PlanDay planDay =
      planDayRepository
        .findByIdAndPlanIdAndPlanUserId(planDayId, planId, user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Plan", planDayId.toString()));

    // Check if day has any completed sessions
    boolean hasCompletedSessions =
      sessionRepository.existsByPlanDayIdAndStatus(planDayId, SessionStatus.COMPLETED);

    if (hasCompletedSessions) {
      throw new ValidationException("Cannot delete day with completed sessions");
    }
    plan.getPlanDays().remove(planDay);
    planDayRepository.delete(planDay);

    log.info("Successfully removed day from plan: {}", planDayId);
  }

  @Transactional
  @Override
  public PlanDayResponse getPlanDayDetails(Authentication authentication, UUID planId, UUID planDayId) {

    User user = (User) authentication.getPrincipal();

    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanIdAndPlanUserId(planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan day not found"));

    int estimatedMinutes = planDay.getPlanItems() != null
        ? planDay.getPlanItems().stream().mapToInt(this::estimateExerciseDuration).sum()
        : 0;
    boolean isCompleted = sessionRepository.existsByPlanDayIdAndStatus(planDay.getId(), SessionStatus.COMPLETED);
    return planMapper.toPlanDayResponse(planDay, true, estimatedMinutes, isCompleted);
  }

  @Transactional
  @Override
  public PlanDayResponse updatePlanDay(
      Authentication authentication, UUID planId, UUID planDayId, UpdatePlanDayRequest request) {

    User user = (User) authentication.getPrincipal();

    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanIdAndPlanUserId(planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan day not found"));

    if (request.getSplitName() != null) {
      planDay.setSplitName(request.getSplitName());
    }

    if (request.getScheduledDate() != null) {
      planDay.setScheduledDate(request.getScheduledDate());
    }

    if (request.getDayIndex() != null) {
      planDay.setDayIndex(request.getDayIndex());
    }

    planDay = planDayRepository.save(planDay);

    log.info("Successfully updated plan day: {}", planDayId);
    int estimatedMinutes = planDay.getPlanItems() != null
        ? planDay.getPlanItems().stream().mapToInt(this::estimateExerciseDuration).sum()
        : 0;
    boolean isCompleted = sessionRepository.existsByPlanDayIdAndStatus(planDay.getId(), SessionStatus.COMPLETED);
    return planMapper.toPlanDayResponse(planDay, true, estimatedMinutes, isCompleted);
  }

  @Transactional
  @Override
  public PlanItemResponse addItemToPlanDay(
      Authentication authentication, UUID planId, UUID planDayId, CreateCustomPlanItemRequest request) {

    User user = (User) authentication.getPrincipal();

    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanIdAndPlanUserId(planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan day not found"));

    Exercise exercise =
        exerciseRepository
            .findById(request.getExerciseId())
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));

    PlanItem planItem = new PlanItem();
    planItem.setPlanDay(planDay);
    planItem.setExercise(exercise);
    planItem.setItemIndex(request.getItemIndex());
    planItem.setPrescription(prescriptionMapper.toJsonNode(request.getPrescription()));
    planItem.setNotes(request.getNotes());

    planItem = planItemRepository.save(planItem);

    log.info("Successfully added item to plan day: {}", planDayId);
    ExerciseDetailResponse exerciseDetail = planItem.getExercise() != null
        ? exerciseMapper.toExerciseDetailResponse(planItem.getExercise())
        : null;
    int timesCompleted = sessionRepository.countCompletedSessionsByPlanItemId(planItem.getId());
    return planMapper.toPlanItemResponseWithDetails(planItem, exerciseDetail, timesCompleted);
  }

  @Transactional
  @Override
  public PlanItemResponse updatePlanItem(
      Authentication authentication, UUID planId, UUID planDayId, UUID planItemId, UpdatePlanItemRequest request) {

    User user = (User) authentication.getPrincipal();

    PlanItem planItem =
        planItemRepository
            .findByIdAndPlanDayIdAndPlanDayPlanIdAndPlanDayPlanUserId(
                planItemId, planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan item not found"));

    if (request.getExerciseId() != null) {
      Exercise exercise =
          exerciseRepository
              .findById(request.getExerciseId())
              .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
      planItem.setExercise(exercise);
    }

    if (request.getItemIndex() != null) {
      planItem.setItemIndex(request.getItemIndex());
    }

    if (request.getPrescription() != null) {
      planItem.setPrescription(prescriptionMapper.toJsonNode(request.getPrescription()));
    }

    if (request.getNotes() != null) {
      planItem.setNotes(request.getNotes());
    }

    planItem = planItemRepository.save(planItem);

    log.info("Successfully updated plan item: {}", planItemId);
    ExerciseDetailResponse exerciseDetail = planItem.getExercise() != null
        ? exerciseMapper.toExerciseDetailResponse(planItem.getExercise())
        : null;
    int timesCompleted = sessionRepository.countCompletedSessionsByPlanItemId(planItem.getId());
    return planMapper.toPlanItemResponseWithDetails(planItem, exerciseDetail, timesCompleted);
  }

  @Transactional
  @Override
  public void removePlanItem(Authentication authentication, UUID planId, UUID planDayId, UUID planItemId) {

    User user = (User) authentication.getPrincipal();

    PlanItem planItem =
        planItemRepository
            .findByIdAndPlanDayIdAndPlanDayPlanIdAndPlanDayPlanUserId(
                planItemId, planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan item not found"));

    // Check if item has any completed sessions
    boolean hasCompletedSessions =
        sessionRepository.existsBySessionSets_PlanItem_IdAndStatus(
            planItemId, SessionStatus.COMPLETED);

    if (hasCompletedSessions) {
      throw new ValidationException("Cannot delete item with completed sessions");
    }

    planItemRepository.delete(planItem);
    log.info("Successfully removed plan item: {}", planItemId);
  }

  @Transactional
  @Override
  public void addMultipleItemsToPlanDay(
      Authentication authentication, UUID planId, UUID planDayId, AddMultipleItemsRequest request) {

    User user = (User) authentication.getPrincipal();

    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanIdAndPlanUserId(planDayId, planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan day not found"));

    // Validate exercises
    Set<UUID> exerciseIds =
        request.getPlanItems().stream()
            .map(CreateCustomPlanItemRequest::getExerciseId)
            .collect(Collectors.toSet());

    Map<UUID, Exercise> exerciseMap =
        exerciseRepository.findAllById(exerciseIds).stream()
            .collect(Collectors.toMap(Exercise::getId, e -> e));

    if (exerciseMap.size() != exerciseIds.size()) {
      throw new ValidationException("Some exercises not found");
    }

    // Create plan items
    for (CreateCustomPlanItemRequest itemRequest : request.getPlanItems()) {
      Exercise exercise = exerciseMap.get(itemRequest.getExerciseId());

      PlanItem planItem = new PlanItem();
      planItem.setPlanDay(planDay);
      planItem.setExercise(exercise);
      planItem.setItemIndex(itemRequest.getItemIndex());
      planItem.setPrescription(prescriptionMapper.toJsonNode(itemRequest.getPrescription()));
      planItem.setNotes(itemRequest.getNotes());

      planItemRepository.save(planItem);
    }
  }

  @Override
  @Transactional
  public PlanDay duplicatePlanDayForNextWeek(PlanDay planDay) {
    if (planDay != null) {

      LocalDate nextScheduledDate = planDay.getScheduledDate().plusWeeks(1);
      if (nextScheduledDate.isAfter(planDay.getPlan().getEndDate())) {
        throw new BusinessException("Plan day end date");
      }

      PlanDay newPlanDay = new PlanDay();
      newPlanDay.setPlan(planDay.getPlan());
      newPlanDay.setDayIndex(planDay.getDayIndex() + 7); // Next week
      newPlanDay.setSplitName(planDay.getSplitName());
      if (planDay.getScheduledDate() != null) {
        newPlanDay.setScheduledDate(nextScheduledDate);
      }

      newPlanDay = planDayRepository.save(newPlanDay);

      List<PlanItem> newPlanItems = new ArrayList<>();
      if (planDay.getPlanItems() != null) {
        for (PlanItem item : planDay.getPlanItems()) {
          PlanItem newItem = new PlanItem();
          newItem.setPlanDay(newPlanDay);
          newItem.setExercise(item.getExercise());
          newItem.setItemIndex(item.getItemIndex());
          newItem.setPrescription(item.getPrescription());
          newItem.setNotes(item.getNotes());

          newPlanItems.add(planItemRepository.save(newItem));
        }
      }

      newPlanDay.setPlanItems(newPlanItems);
      return newPlanDay;
    }
    return null;
  }

  @Override
  public void usePlan(UUID planId) {
    Plan plan = planRepository.findById(planId)
      .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));

    List<PlanDay> planDays = planDayRepository.findAllByPlanId(plan.getId());

    LocalDate currentDate = LocalDate.now();
    planDays.sort(Comparator.comparingInt(PlanDay::getDayIndex));

    for (int i = 0; i < planDays.size(); i++) {
      planDays.get(i).setScheduledDate(currentDate.plusDays(i));
    }

    plan.setEndDate(currentDate.plusWeeks(plan.getCycleWeeks()));
    plan.setStatus(PlanStatusType.ACTIVE);
    planRepository.save(plan);
  }

  // Helper methods

  private int estimateExerciseDuration(PlanItem planItem) {
    try {
      JsonNode prescription = planItem.getPrescription();
      int sets = prescription.get("sets").asInt();
      int restSeconds =
          prescription.has("restSeconds") ? prescription.get("restSeconds").asInt() : 90;

      // Rough estimate: 30 seconds per set + rest time
      return (sets * 30 + (sets - 1) * restSeconds) / 60; // Convert to minutes
    } catch (Exception e) {
      return 5; // Default 5 minutes per exercise
    }
  }

}
