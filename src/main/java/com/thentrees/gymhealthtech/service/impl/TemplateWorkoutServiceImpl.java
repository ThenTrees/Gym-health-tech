package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.TemplateItemResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutDayResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.service.RedisService;
import com.thentrees.gymhealthtech.service.TemplateWorkoutService;
import com.thentrees.gymhealthtech.service.UserService;
import com.thentrees.gymhealthtech.util.CacheKeyUtils;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static com.thentrees.gymhealthtech.constant.S3Constant.S3_IMAGE_POST_FOLDER;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TEMPLATE_WORKOUT-SERVICE")
public class TemplateWorkoutServiceImpl implements TemplateWorkoutService {

  private final TemplateWorkoutRepository templateWorkoutRepository;
  private final ExerciseRepository exerciseRepository;
  private final S3Util s3Util;
  private final FileValidator fileValidator;
  private final TemplateDayRepository templateDayRepository;
  private final TemplateItemRepository templateItemRepository;
  private final UserService userService;
  private final ObjectMapper objectMapper;
  private final PlanRepository planRepository;
  private final SessionRepository sessionRepository;
  private final RedisService redisService;
  private final CacheKeyUtils cacheKeyUtils;

  @Transactional
  @Override
  public TemplateWorkoutResponse createTemplateWorkout(CreateTemplateRequest request, MultipartFile file) {
    redisService.deletePattern("template:*");

    log.info("Create template workout: {}", request.toString());

    String fileUrl = this.uploadImageBanner(file);
    WorkoutTemplate workoutTemplate = mapToEntity(request, fileUrl);
    List<TemplateDay> templateDays = request.getTemplateDays().stream().map(templateDayRequest -> {
        TemplateDay templateDay = TemplateDay.builder()
          .workoutTemplate(workoutTemplate)
          .dayName(templateDayRequest.getDayName())
          .dayOrder(templateDayRequest.getDayOrder())
          .dayOfWeek(templateDayRequest.getDayOfWeek())
          .durationMinutes(templateDayRequest.getDurationMinutes())
          .notes(templateDayRequest.getNotes())
          .build();

        List<TemplateItem> items = templateDayRequest.getTemplateItems().stream().map(
          itemRequest -> {
            Exercise ex = exerciseRepository.findById(itemRequest.getExerciseId()).orElseThrow(
              () -> new ResourceNotFoundException("Exercise", itemRequest.getExerciseId().toString())
            );
            return TemplateItem.builder()
              .exercise(ex)
              .itemOrder(itemRequest.getItemOrder())
              .sets(itemRequest.getSets())
              .reps(itemRequest.getReps())
              .restSeconds(itemRequest.getRestSeconds())
              .notes(itemRequest.getNotes())
              .templateDay(templateDay)
              .build();
          }).toList();
        templateDay.setTemplateItems(items);
        templateDay.setTotalExercises(items.size());
        return templateDay;
      }).toList();

    workoutTemplate.setTemplateDays(templateDays);

    WorkoutTemplate saved = templateWorkoutRepository.save(workoutTemplate);

    return mapToTemplateWorkoutResponse(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public TemplateWorkoutResponse getTemplateWorkoutById(UUID id) {

    String cacheKey = cacheKeyUtils.buildKey("template:", id);
    Object cached = redisService.get(cacheKey);
    if (cached != null) {
      TemplateWorkoutResponse result = objectMapper.convertValue(cached, TemplateWorkoutResponse.class);
      if (result != null)
        return result;
    }
    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(id).orElseThrow(
      ()-> new ResourceNotFoundException("TemplateWorkout", id.toString()));

    TemplateWorkoutResponse res = mapToTemplateWorkoutResponse(workoutTemplate);

    redisService.set(cacheKey, res);

    return res;
  }

  @Transactional(readOnly = true)
  @Override
  public List<TemplateWorkoutResponse> getTemplateWorkouts() {

    String cacheKey = cacheKeyUtils.buildKey("template:", "all");

    Object cached = redisService.get(cacheKey);

    if (cached != null) {
      List<TemplateWorkoutResponse> result = objectMapper.convertValue(cached, new TypeReference<List<TemplateWorkoutResponse>>() {
      });
      if (result != null)
        return result;
    }

    List<WorkoutTemplate> templateWorkouts = templateWorkoutRepository.findAll();

    List<TemplateWorkoutResponse> res = templateWorkouts.stream().map(this::mapToTemplateWorkoutResponse).toList();

    redisService.set(cacheKey, res);

    return res;
  }

  @Override
  public TemplateWorkoutResponse updateTemplateWorkout(UUID id, CreateTemplateRequest request, MultipartFile file) {

    redisService.deletePattern("template:*");

    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(id).orElseThrow(
      () -> new ResourceNotFoundException("TemplateWorkout", id.toString())
    );
    if (workoutTemplate.getThumbnailUrl() != null) {
      s3Util.deleteFileByKey(workoutTemplate.getThumbnailUrl());
    }
    uploadImageBanner(file);

    workoutTemplate.setName(request.getName());
    workoutTemplate.setDescription(request.getDescription());
    workoutTemplate.setObjective(request.getObjective());
    workoutTemplate.setDurationWeeks(request.getDurationWeeks());
    workoutTemplate.setSessionsPerWeek(request.getSessionsPerWeek());

    WorkoutTemplate saved = templateWorkoutRepository.save(workoutTemplate);

    return mapToTemplateWorkoutResponse(saved);
  }

  @Override
  public void deleteTemplateWorkoutById(UUID id) {

    redisService.deletePattern("template:*");

    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(id).orElseThrow(
      () -> new ResourceNotFoundException("TemplateWorkout", id.toString())
    );
    workoutTemplate.setIsActive(false);
    workoutTemplate.markAsDeleted();
    templateWorkoutRepository.save(workoutTemplate);
    log.info("Del Template workout has id : {}", workoutTemplate.getId());
  }

  @Override
  public void activeTemplateWorkoutById(UUID id) {

    redisService.deletePattern("template:*");

    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findById(id).orElseThrow(
      () -> new ResourceNotFoundException("TemplateWorkout", id.toString())
    );
    workoutTemplate.setIsActive(true);
    workoutTemplate.restore();
    templateWorkoutRepository.save(workoutTemplate);
  }

  @Transactional
  @Override
  public TemplateWorkoutResponse addTemplateDayToTemplate(UUID templateId, CreateTemplateDayRequest request) {

    redisService.deletePattern("template:*");

    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(templateId).orElseThrow(
      ()-> new ResourceNotFoundException("TemplateWorkout", templateId.toString()));

    TemplateDay templateDay = mapToTemplateDayEntity(request);
    templateDay.setWorkoutTemplate(workoutTemplate);

    List<TemplateItem> templateItems = request.getTemplateItems().stream()
      .map(this::mapToTemplateItemEntity)
      .peek(templateItem -> templateItem.setTemplateDay(templateDay))
      .toList();

    templateDay.setTemplateItems(templateItems);

    workoutTemplate.getTemplateDays().add(templateDay);
    templateWorkoutRepository.save(workoutTemplate);
    return mapToTemplateWorkoutResponse(workoutTemplate);
  }

  @Transactional
  @Override
  public TemplateWorkoutDayResponse addTemplateItemToTemplateDay(UUID templateDayId, CreateTemplateItemRequest request) {
    TemplateDay templateDay = templateDayRepository.findById(templateDayId).orElseThrow(
      ()-> new ResourceNotFoundException("TemplateDay", templateDayId.toString()));
    TemplateItem templateItem = mapToTemplateItemEntity(request);
    templateItem.setTemplateDay(templateDay);
    templateDay.getTemplateItems().add(templateItem);
    templateDayRepository.save(templateDay);
    return mapToTemplateWorkoutDayResponse(templateDay);
  }

  @Override
  public void removeTemplateItem(UUID templateItemId) {

    redisService.deletePattern("template:*");

    TemplateItem item = templateItemRepository.findById(templateItemId).orElseThrow(
      ()->new ResourceNotFoundException("TemplateItem", templateItemId.toString()));
    templateItemRepository.delete(item);
    log.info("Remove template item: {} successfully!", templateItemId);
  }

  @Override
  public void removeTemplateDay(UUID templateDayId) {
    TemplateDay templateDay = templateDayRepository.findById(templateDayId).orElseThrow(
      ()-> new ResourceNotFoundException("TemplateDay", templateDayId.toString())
    );
    templateDayRepository.delete(templateDay);
    log.info("Remove template day: {} successfully!", templateDayId);
  }

  @Transactional
  @Override
  public void applyTemplateWorkout(UUID templateId) {

    User user = getCurrentUser();

    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(templateId)
      .orElseThrow(() -> new ResourceNotFoundException("TemplateWorkout", templateId.toString()));

    // pause existing plans/sessions...
    List<Plan> plans = planRepository.findByUserId(user.getId());
    if (!plans.isEmpty()) {
      plans.forEach(plan -> plan.setStatus(PlanStatusType.PAUSE));
      planRepository.saveAll(plans);
    }
    Session session = sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.IN_PROGRESS).orElse(null);
    if (session != null) {
      session.setStatus(SessionStatus.PAUSED);
      sessionRepository.save(session);
    }

    Plan newPlan = Plan.builder()
      .user(user)
      .title(workoutTemplate.getName())
      .description(workoutTemplate.getDescription())
      .cycleWeeks(workoutTemplate.getDurationWeeks())
      .source(PlanSourceType.TEMPLATE)
      .status(PlanStatusType.ACTIVE)
      .endDate(LocalDate.now().plusWeeks(workoutTemplate.getDurationWeeks()))
      .build();

    LocalDate startDate = LocalDate.now();

    // ensure templateDays are in expected order (dayOrder ascending)
    List<TemplateDay> templateDays = workoutTemplate.getTemplateDays().stream()
      .sorted(Comparator.comparingInt(TemplateDay::getDayOrder))
      .toList();

    List<PlanDay> planDays = new ArrayList<>();
    int dayIndexCounter = 1;

    // Precompute baseScheduled for each templateDay (the first occurrence >= startDate)
    Map<TemplateDay, LocalDate> baseScheduledMap = new HashMap<>();
    for (TemplateDay td : templateDays) {
      DayOfWeek dow = DayOfWeek.of(td.getDayOfWeek()); // assuming getDayOfWeek() returns 1..7
      LocalDate base = startDate.with(TemporalAdjusters.nextOrSame(dow));
      // if base is before startDate (shouldn't happen because nextOrSame), but keep safe:
      if (base.isBefore(startDate)) {
        base = base.plusWeeks(1);
      }
      baseScheduledMap.put(td, base);
    }

    // For each week, clone templateDays with proper week offset
    for (int week = 0; week < workoutTemplate.getDurationWeeks(); week++) {
      for (TemplateDay templateDay : templateDays) {
        LocalDate scheduled = baseScheduledMap.get(templateDay).plusWeeks(week);

        PlanDay planDay = PlanDay.builder()
          .plan(newPlan)
          .dayIndex(dayIndexCounter++)      // unique across whole plan
          .splitName(templateDay.getDayName())
          .createdAt(OffsetDateTime.now())
          .scheduledDate(scheduled)
          .build();

        List<PlanItem> planItems = templateDay.getTemplateItems().stream()
          .sorted(Comparator.comparingInt(TemplateItem::getItemOrder))
          .map(templateItem -> PlanItem.builder()
            .planDay(planDay)
            .exercise(templateItem.getExercise())
            .itemIndex(templateItem.getItemOrder())
            .notes(templateItem.getNotes())
            .prescription(convertPrescriptionToJson(
              templateItem.getSets(),
              templateItem.getReps(),
              templateItem.getRestSeconds()))
            .createdAt(OffsetDateTime.now())
            .build())
          .toList();

        planDay.setPlanItems(planItems);
        planDays.add(planDay);
      }
    }

    newPlan.setPlanDays(planDays);
    planRepository.save(newPlan);
  }


  private TemplateWorkoutResponse mapToTemplateWorkoutResponse(WorkoutTemplate entity){

    List<TemplateWorkoutDayResponse> templateWorkoutDays = entity.getTemplateDays().stream().map(
      this::mapToTemplateWorkoutDayResponse
    ).toList();

    int totalExercise = templateWorkoutDays.stream()
      .mapToInt(TemplateWorkoutDayResponse::getTotalExercises)
      .sum();

    return TemplateWorkoutResponse.builder()
      .id(entity.getId())
      .name(entity.getName())
      .description(entity.getDescription())
      .goal(entity.getObjective().toString())
      .totalWeek(entity.getDurationWeeks())
      .sessionPerWeek(entity.getSessionsPerWeek())
      .thumbnailUrl(entity.getThumbnailUrl())
      .totalUsed(entity.getTotalUsed())
      .totalExercise(totalExercise)
      .isActive(entity.getIsActive())
      .templateWorkoutDay(templateWorkoutDays)
      .build();
  }

  private WorkoutTemplate mapToEntity(CreateTemplateRequest request, String file){
    return WorkoutTemplate.builder()
      .name(request.getName())
      .description(request.getDescription())
      .objective(request.getObjective())
      .durationWeeks(request.getDurationWeeks())
      .sessionsPerWeek(request.getSessionsPerWeek())
      .thumbnailUrl(file)
      .isActive(true)
      .build();
  }

  private TemplateItem mapToTemplateItemEntity(CreateTemplateItemRequest request){
    Exercise ex = exerciseRepository.findById(request.getExerciseId()).orElseThrow(
      () -> new ResourceNotFoundException("Exercise", request.getExerciseId().toString())
    );
    return TemplateItem.builder()
      .exercise(ex)
      .itemOrder(request.getItemOrder())
      .sets(request.getSets())
      .reps(request.getReps())
      .restSeconds(request.getRestSeconds())
      .notes(request.getNotes())
      .build();
  }

  private TemplateDay mapToTemplateDayEntity(CreateTemplateDayRequest request){
    return TemplateDay.builder()
      .dayName(request.getDayName())
      .dayOfWeek(request.getDayOfWeek())
      .dayOrder(request.getDayOrder())
      .durationMinutes(request.getDurationMinutes())
      .notes(request.getNotes())
      .build();
  }

  private String uploadImageBanner(MultipartFile file){
    fileValidator.validateImage(file);
    String bannerUrl = null;
    try {
      bannerUrl = s3Util.uploadFile(file, S3_IMAGE_POST_FOLDER);
      return bannerUrl;
    } catch (Exception e) {
      log.error("Error uploading banner post image", e);
      if (bannerUrl != null) s3Util.deleteFileByUrl(bannerUrl);
      throw new BusinessException("Failed to upload banner post image", e.getMessage());
    }
  }

  private TemplateWorkoutDayResponse mapToTemplateWorkoutDayResponse(TemplateDay templateDay){
    List<TemplateItemResponse> templateItemResponses =
      templateDay.getTemplateItems().stream().map(this::mapToTemplateItemResponse).toList();
      return TemplateWorkoutDayResponse.builder()
        .dayName(templateDay.getDayName())
        .dayOrder(templateDay.getDayOrder())
        .durationMinutes(templateDay.getDurationMinutes())
        .dayOfWeek(templateDay.getDayOfWeek())
        .notes(templateDay.getNotes())
        .totalExercises(templateItemResponses.size())
        .templateItems(templateItemResponses)
        .build();
  }

  private TemplateItemResponse mapToTemplateItemResponse(TemplateItem templateItem){
    return TemplateItemResponse.builder()
      .id(templateItem.getId())
      .itemOrder(templateItem.getItemOrder())
      .sets(templateItem.getSets())
      .reps(templateItem.getReps())
      .restSeconds(templateItem.getRestSeconds())
      .notes(templateItem.getNotes())
      .exerciseId(templateItem.getExercise().getId())
      .exerciseName(templateItem.getExercise().getName())
      .exerciseType(templateItem.getExercise().getExerciseType().toString())
      .bodyPart(templateItem.getExercise().getBodyPart())
      .thumbnailUrl(templateItem.getExercise().getThumbnailUrl())
      .build();
  }

  private JsonNode convertPrescriptionToJson(
    Integer sets, Integer reps, Integer restSeconds) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("sets", sets);
    node.put("reps", reps);
    node.put("restSeconds", restSeconds);
    return node;
  }

  private User getCurrentUser(){
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (User) auth.getPrincipal();
  }
}
