package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.common.ExerciseType;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.ExerciseMapper;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.repository.spec.ExerciseSpecifications;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseLibraryServiceImpl implements ExerciseLibraryService {

  private final ExerciseRepository exerciseRepository;
  private final ExerciseMuscleRepository exerciseMuscleRepository;
  private final ExerciseMapper exerciseMapper;
  private final MuscleRepository muscleRepository;
  private final EquipmentTypeRepository equipmentTypeRepository;
  private final ExerciseCategoryRepository exerciseCategoryRepository;
  private final Validator validator;
  private final ObjectMapper objectMapper;

  @Override
  public PagedResponse<ExerciseListResponse> getExercises(ExerciseSearchRequest request) {
    Specification<Exercise> spec = buildSearchSpecification(request);

    // Create pageable
    Pageable pageable =
        PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.Direction.fromString(request.getSortDirection()),
            request.getSortBy());

    Page<Exercise> exercises = exerciseRepository.findAll(spec, pageable);
    Page<ExerciseListResponse> exerciseListResponsePage = exercises.map(this::mapToListResponse);
    return PagedResponse.of(exerciseListResponsePage);
  }

  @Override
  @Transactional
  public ExerciseDetailResponse createExercise(CreateExerciseRequest request, Authentication auth) {
    log.info("Starting create exercise process");
    String slug = request.getName().trim().toLowerCase().replace(" ", "-");

    // Validate slug uniqueness
    if (exerciseRepository.existsBySlug(slug)) {
      throw new IllegalArgumentException("Exercise with slug '" + slug + "' already exists");
    }

    // Validate equipment (optional)
    EquipmentType equipment = null;
    if (request.getEquipmentTypeCode() != null) {
      equipment =
          equipmentTypeRepository
              .findById(request.getEquipmentTypeCode())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Equipment type not found: " + request.getEquipmentTypeCode()));
    }

    ExerciseCategory exerciseCategory = null;
    if (request.getExerciseCategory() != null) {
      exerciseCategory =
          exerciseCategoryRepository
              .findById(request.getExerciseCategory())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Exercise type not found: " + request.getExerciseCategory()));
    }

    // Create exercise
    Exercise exercise = new Exercise();
    exercise.setSlug(slug);
    exercise.setName(request.getName());
    //    exercise.setLevel(request.getExerciseLevel());
    exercise.setExerciseCategory(exerciseCategory);
    exercise.setEquipment(equipment);
    exercise.setInstructions(request.getInstructions().toString());
    exercise.setSafetyNotes(request.getSafetyNotes());
    exercise.setThumbnailUrl(request.getThumbnailUrl());
    Exercise savedExercise = exerciseRepository.save(exercise);

    // Add muscles
    if (request.getMuscles() != null && !request.getMuscles().isEmpty()) {
      saveMusclesForExercise(savedExercise, request.getMuscles());
    }

    return mapToDetailResponse(savedExercise);
  }

  @Override
  @Transactional
  public int importExercisesFromJson(MultipartFile file) throws IOException {
    // đọc mảng ExerciseDTO từ JSON upload
    List<CreateExerciseRequest> dtos =
        objectMapper.readValue(
            file.getInputStream(), new TypeReference<List<CreateExerciseRequest>>() {});

    int imported = 0;
    for (CreateExerciseRequest dto : dtos) {
      if (dto.getName() == null || dto.getName().isBlank()) continue;
      log.info("name exercise: {}", dto.getName());
      String slug = generateSlug(dto.getName());

      Exercise ex = exerciseRepository.findBySlug(slug).orElseGet(Exercise::new);
      ex.setSlug(slug);
      ex.setName(dto.getName().trim());

      EquipmentType equipmentType = null;

      if (dto.getEquipmentTypeCode() != null) {
        equipmentType =
            equipmentTypeRepository
                .findById(dto.getEquipmentTypeCode())
                .orElseThrow(
                    () ->
                        new ResourceNotFoundException(
                            "Equipment type not found: " + dto.getEquipmentTypeCode()));
      }

      ex.setEquipment(equipmentType);

      ExerciseCategory exerciseCategory =
          dto.getExerciseCategory() == null
              ? null
              : exerciseCategoryRepository
                  .findById(dto.getExerciseCategory())
                  .orElseThrow(
                      () ->
                          new ResourceNotFoundException(
                              "Exercise category not found with code: {}",
                              dto.getExerciseCategory()));

      ex.setExerciseCategory(exerciseCategory);
      ex.setExerciseType(ExerciseType.valueOf(dto.getExerciseType()));

      //      ex.setLevel(dto.getExerciseLevel());
      ex.setInstructions(dto.getInstructions().toString());
      ex.setSafetyNotes(dto.getSafetyNotes());
      ex.setThumbnailUrl(dto.getThumbnailUrl());
      Exercise savedExercise = exerciseRepository.save(ex);
      // Add muscles
      if (dto.getMuscles() != null && !dto.getMuscles().isEmpty()) {
        saveMusclesForExercise(savedExercise, dto.getMuscles());
      }
      log.info("imported: {}", imported);

      imported++;
    }
    return imported;
  }

  private ExerciseListResponse mapToListResponse(Exercise exercise) {
    // Get secondary muscles
    List<ExerciseMuscle> muscles =
        exerciseMuscleRepository.findByExerciseIdOrderByRole(exercise.getId());

    List<String> secondaryMuscles =
        muscles.stream()
            .filter(em -> "SECONDARY".equals(em.getRole()))
            .map(em -> em.getMuscle().getName())
            .collect(Collectors.toList());

    return ExerciseListResponse.builder()
        .id(exercise.getId())
        .slug(exercise.getSlug())
        .name(exercise.getName())
        //        .level(exercise.getLevel())
        .primaryMuscle(
            exercise.getPrimaryMuscle() != null
                ? exerciseMapper.toMuscleResponse(exercise.getPrimaryMuscle())
                : null)
        .equipment(
            exercise.getEquipment() != null
                ? exerciseMapper.toEquipmentTypeResponse(exercise.getEquipment())
                : null)
        .thumbnailUrl(exercise.getThumbnailUrl())
        .createdAt(exercise.getCreatedAt())
        .updatedAt(exercise.getUpdatedAt())
        .secondaryMuscles(secondaryMuscles)
        .build();
  }

  private ExerciseDetailResponse mapToDetailResponse(Exercise exercise) {
    // Get muscles
    List<ExerciseMuscle> muscles =
        exerciseMuscleRepository.findByExerciseIdOrderByRole(exercise.getId());
    List<ExerciseMuscleResponse> muscleResponses =
        muscles.stream()
            .map(
                em ->
                    ExerciseMuscleResponse.builder()
                        .muscleCode(em.getMuscle().getCode())
                        .muscleName(em.getMuscle().getName())
                        .role(em.getRole())
                        .build())
            .collect(Collectors.toList());

    return ExerciseDetailResponse.builder()
        .id(exercise.getId())
        .slug(exercise.getSlug())
        .name(exercise.getName())
        //        .level(exercise.getLevel())
        .equipment(
            exercise.getEquipment() != null
                ? exerciseMapper.toEquipmentTypeResponse(exercise.getEquipment())
                : null)
        .instructions(exercise.getInstructions())
        .safetyNotes(exercise.getSafetyNotes())
        .thumbnailUrl(exercise.getThumbnailUrl())
        .createdAt(exercise.getCreatedAt())
        .updatedAt(exercise.getUpdatedAt())
        .createdBy(exercise.getCreatedBy())
        .updatedBy(exercise.getUpdatedBy())
        .muscles(muscleResponses)
        .build();
  }

  private Specification<Exercise> buildSearchSpecification(ExerciseSearchRequest request) {
    return ExerciseSpecifications.hasKeyword(request.getKeyword())
        .and(ExerciseSpecifications.hasLevel(request.getLevel()))
        .and(ExerciseSpecifications.hasPrimaryMuscle(request.getPrimaryMuscle()))
        .and(ExerciseSpecifications.hasEquipment(request.getEquipmentType()))
        .and(ExerciseSpecifications.hasMuscles(request.getMusclesCodes()))
        .and(ExerciseSpecifications.hasExerciseType(request.getExerciseType()));
  }

  // Private helper methods
  private void saveMusclesForExercise(
      Exercise exercise, List<ExerciseMuscleRequest> muscleRequests) {
    // Validate all muscles exist
    List<String> muscleCodes =
        muscleRequests.stream()
            .map(ExerciseMuscleRequest::getMuscleCode)
            .collect(Collectors.toList());

    List<Muscle> muscles = muscleRepository.findByCodes(muscleCodes);
    if (muscles.size() != muscleCodes.size()) {
      throw new ResourceNotFoundException("Some muscle codes not found");
    }

    // Create exercise muscles
    for (ExerciseMuscleRequest muscleReq : muscleRequests) {
      ExerciseMuscle exerciseMuscle = new ExerciseMuscle();

      ExerciseMuscleId id = new ExerciseMuscleId();
      id.setExerciseId(exercise.getId());
      id.setMuscleCode(muscleReq.getMuscleCode());

      exerciseMuscle.setId(id);
      exerciseMuscle.setExercise(exercise);
      exerciseMuscle.setMuscle(
          muscles.stream()
              .filter(m -> m.getCode().equals(muscleReq.getMuscleCode()))
              .findFirst()
              .orElseThrow());
      exerciseMuscle.setRole(muscleReq.getRole());

      exerciseMuscleRepository.save(exerciseMuscle);
    }
  }

  private ExerciseCategory getOrCreateCategory(String categoryCode) {
    if (!StringUtils.hasText(categoryCode)) {
      categoryCode = "General";
    }

    String finalCategoryCode = categoryCode;
    return exerciseCategoryRepository
        .findByCodeWithExercises(categoryCode)
        .orElseGet(
            () -> {
              ExerciseCategory newCategory = new ExerciseCategory();
              newCategory.setCode(finalCategoryCode);
              newCategory.setName(StringUtils.capitalize(finalCategoryCode.toLowerCase()));
              return exerciseCategoryRepository.save(newCategory);
            });
  }

  private Exercise mapToEntity(CreateExerciseRequest dto, ExerciseCategory category) {
    Exercise exercise = new Exercise();

    exercise.setName(dto.getName());
    exercise.setExerciseCategory(category);
    exercise.setBodyPart(dto.getBodyParts().toString());

    EquipmentType equipmentType =
        equipmentTypeRepository
            .findById(dto.getEquipmentTypeCode())
            .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));

    exercise.setEquipment(equipmentType);

    ExerciseMuscleRequest exerciseMusclePrimaryRequest =
        dto.getMuscles().stream()
            .filter(muscle -> muscle.getRole().equalsIgnoreCase("PRIMARY"))
            .findFirst()
            .get();

    Muscle primaryMuscle =
        muscleRepository.findByCode(exerciseMusclePrimaryRequest.getMuscleCode());
    exercise.setPrimaryMuscle(primaryMuscle);

    // Handle instructions
    if (dto.getInstructions() != null) {
      exercise.setInstructions(dto.getInstructions().toString());
    }

    exercise.setThumbnailUrl(dto.getThumbnailUrl());

    // Parse exercise type
    if (StringUtils.hasText(dto.getExerciseType().toString())) {
      try {
        exercise.setExerciseType(
            ExerciseType.valueOf(dto.getExerciseType().toString().toUpperCase()));
      } catch (IllegalArgumentException e) {
        exercise.setExerciseType(ExerciseType.COMPOUND);
      }
    }

    return exercise;
  }

  private String generateSlug(String name) {
    return name.trim().toLowerCase().replace(" ", "-");
  }
}
