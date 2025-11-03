package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateTemplateDayRequest;
import com.thentrees.gymhealthtech.dto.request.CreateTemplateItemRequest;
import com.thentrees.gymhealthtech.dto.request.CreateTemplateRequest;
import com.thentrees.gymhealthtech.dto.response.TemplateItemResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutDayResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.Exercise;
import com.thentrees.gymhealthtech.model.TemplateDay;
import com.thentrees.gymhealthtech.model.TemplateItem;
import com.thentrees.gymhealthtech.model.WorkoutTemplate;
import com.thentrees.gymhealthtech.repository.ExerciseRepository;
import com.thentrees.gymhealthtech.repository.TemplateWorkoutRepository;
import com.thentrees.gymhealthtech.service.TemplateWorkoutService;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.thentrees.gymhealthtech.constant.S3Constant.S3_IMAGE_POST_FOLDER;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TEMPLATE_WORKOUT-SERVICE")
public class TemplateWorkoutServiceImpl implements TemplateWorkoutService {

  private final TemplateWorkoutRepository templateWorkoutRepository;
  private final ExerciseRepository exerciseRepository;
  private final S3Util s3Util;
  private final FileValidator fileValidator;

  @Transactional
  @Override
  public TemplateWorkoutResponse createTemplateWorkout(CreateTemplateRequest request, MultipartFile file) {

    //TODO: xoá cache

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
    // TODO:impl cache
    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findByIdAndIsActiveTrue(id).orElseThrow(
      ()-> new ResourceNotFoundException("TemplateWorkout", id.toString()));

    return mapToTemplateWorkoutResponse(workoutTemplate);
  }


  @Transactional(readOnly = true) // co the dung fetch join
  @Override
  public List<TemplateWorkoutResponse> getTemplateWorkouts() {
    //TODO: impl cache
    List<WorkoutTemplate> templateWorkouts = templateWorkoutRepository.findAllByIsActiveTrue();
    return templateWorkouts.stream().map(this::mapToTemplateWorkoutResponse).toList();
  }

  @Override
  public TemplateWorkoutResponse updateTemplateWorkout(UUID id, CreateTemplateRequest request, MultipartFile file) {
    //TODO: xoá cache về template
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
    //TODO: clear cache
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
    //TODO: clear cache
    WorkoutTemplate workoutTemplate = templateWorkoutRepository.findById(id).orElseThrow(
      () -> new ResourceNotFoundException("TemplateWorkout", id.toString())
    );
    workoutTemplate.setIsActive(true);
    workoutTemplate.restore();
    templateWorkoutRepository.save(workoutTemplate);
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

  private TemplateDay mapToTemplateDayEntity(CreateTemplateDayRequest request, WorkoutTemplate entity){
    return TemplateDay.builder()
      .workoutTemplate(entity)
      .dayName(request.getDayName())
      .dayOfWeek(request.getDayOfWeek())
      .dayOrder(request.getDayOrder())
      .durationMinutes(request.getDurationMinutes())
      .notes(request.getNotes())
      .templateItems(request.getTemplateItems().stream().map(this::mapToTemplateItemEntity).toList())
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
}
