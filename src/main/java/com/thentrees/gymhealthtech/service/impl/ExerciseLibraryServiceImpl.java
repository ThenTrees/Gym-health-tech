package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseMuscleRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.ExerciseMapper;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.repository.spec.ExerciseSpecifications;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseLibraryServiceImpl implements ExerciseLibraryService {

  private final ExerciseRepository exerciseRepository;
  private final ExerciseMuscleRepository exerciseMuscleRepository;
  private final ExerciseMapper exerciseMapper;
  private final MuscleRepository muscleRepository;
  private final EquipmentTypeRepository equipmentTypeRepository;
  private final ExerciseTypeRepository exerciseTypeRepository;

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

    String slug = request.getName().trim().toLowerCase().replace(" ", "-");

    // Validate slug uniqueness
    if (exerciseRepository.existsBySlug(slug)) {
      throw new IllegalArgumentException("Exercise with slug '" + slug + "' already exists");
    }

    // Validate primary muscle
    Muscle primaryMuscle =
        muscleRepository
            .findById(request.getPrimaryMuscleCode())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Primary muscle not found: " + request.getPrimaryMuscleCode()));

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

    ExerciseType exerciseType = null;
    if (request.getExerciseType() != null) {
      exerciseType =
          exerciseTypeRepository
              .findById(request.getExerciseType())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Exercise type not found: " + request.getExerciseType()));
    }

    // Create exercise
    Exercise exercise = new Exercise();
    exercise.setSlug(slug);
    exercise.setName(request.getName());
    exercise.setLevel(request.getLevel());
    exercise.setPrimaryMuscle(primaryMuscle);
    exercise.setExerciseType(exerciseType);
    exercise.setEquipment(equipment);
    exercise.setInstructions(request.getInstructions());
    exercise.setSafetyNotes(request.getSafetyNotes());
    exercise.setThumbnailUrl(request.getThumbnailUrl());

    Exercise savedExercise = exerciseRepository.save(exercise);

    // Add muscles
    if (request.getMuscles() != null && !request.getMuscles().isEmpty()) {
      saveMusclesForExercise(savedExercise, request.getMuscles());
    }

    return mapToDetailResponse(savedExercise);
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
        .level(exercise.getLevel())
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
        .level(exercise.getLevel())
        .primaryMuscle(
            exercise.getPrimaryMuscle() != null
                ? exerciseMapper.toMuscleResponse(exercise.getPrimaryMuscle())
                : null)
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
}
