package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.ExerciseType;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.ExerciseMapper;
import com.thentrees.gymhealthtech.mapper.MuscleMapper;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.repository.spec.ExerciseSpecification;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
import com.thentrees.gymhealthtech.service.RedisService;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.thentrees.gymhealthtech.util.CacheKeyUtils;
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
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j(topic = "EXERCISE-SERVICE")
@RequiredArgsConstructor
public class ExerciseLibraryServiceImpl implements ExerciseLibraryService {

  private final ExerciseRepository exerciseRepository;
  private final ExerciseMuscleRepository exerciseMuscleRepository;
  private final ExerciseMapper exerciseMapper;
  private final MuscleRepository muscleRepository;
  private final EquipmentRepository equipmentRepository;
  private final ExerciseCategoryRepository exerciseCategoryRepository;
  private final ObjectMapper objectMapper;
  private final MuscleMapper muscleMapper;
  private final ExerciseEquipmentRepository exerciseEquipmentRepository;
  private final RedisService redisService;
  private final CacheKeyUtils cacheKeyUtils;

  @Override
  public PagedResponse<ExerciseListResponse> getExercises(ExerciseSearchRequest request) {

    String cacheKey = cacheKeyUtils.buildKey("exercise:search:", request);

    try {
      Object cached = redisService.get(cacheKey);
      if (cached != null) {
        PagedResponse<ExerciseListResponse> cachedResponse =
          objectMapper.convertValue(cached, new TypeReference<>() {});
        if (cachedResponse != null) {
          log.debug("Cache HIT for key: {}", cacheKey);
          return cachedResponse;
        }
      }
      log.debug("Cache MISS for key: {}", cacheKey);
    } catch (Exception e) {
      log.warn("Failed to read cache for key {}: {}", cacheKey, e.getMessage());
    }

    Specification<Exercise> spec = buildSearchSpecification(request);

    Pageable pageable =
        PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.Direction.fromString(request.getSortDirection()),
            request.getSortBy());

    Page<Exercise> exercises = exerciseRepository.findAll(spec, pageable);
    Page<ExerciseListResponse> exerciseListResponsePage = exercises.map(this::mapToListResponse);
    PagedResponse<ExerciseListResponse> response = PagedResponse.of(exerciseListResponsePage);

    redisService.set(cacheKey, response);

    return response;
  }

  @Override
  public ExerciseDetailResponse getExerciseById(UUID id) {
    return exerciseRepository
        .findByIdAndIsDeletedFalse(id)
        .map(this::mapToDetailResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Exercise", id.toString()));
  }

  @Override
  @Transactional
  public ExerciseDetailResponse createExercise(CreateExerciseRequest request) {
    redisService.deletePattern("exercise:*");
    String slug = request.getName().trim().toLowerCase().replace(" ", "-");
    // Validate slug uniqueness
    if (exerciseRepository.existsBySlug(slug)) {
      throw new IllegalArgumentException("Exercise with slug '" + slug + "' already exists");
    }

    // Validate equipment (optional)
    Equipment equipment = null;
    if (!StringUtils.hasText(request.getEquipmentTypeCode())) {
      equipment = getOrCreateEquipment(request.getEquipmentTypeCode());
    }

    ExerciseCategory exerciseCategory = getOrCreateCategory(request.getExerciseCategory());

    // Create exercise
    Exercise exercise = new Exercise();
    exercise.setSlug(slug);
    exercise.setName(request.getName());
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

    redisService.deletePattern("exercise:*");

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
      ex.setBodyPart(dto.getBodyParts().toString());
      Equipment equipment = null;
      if (StringUtils.hasText(dto.getEquipmentTypeCode())) {
        equipment = getOrCreateEquipment(dto.getEquipmentTypeCode());
      }

      ex.setEquipment(equipment);

      ExerciseCategory exerciseCategory = getOrCreateCategory(dto.getExerciseCategory());

      ex.setDifficultyLevel(difficultyLevelForExercise(dto.getEquipmentTypeCode()));
      ex.setExerciseCategory(exerciseCategory);
      ex.setExerciseType(ExerciseType.valueOf(dto.getExerciseType()));
      ex.setInstructions(dto.getInstructions().toString());
      ex.setSafetyNotes(dto.getSafetyNotes());
      ex.setThumbnailUrl(dto.getThumbnailUrl());
      ex.setPrimaryMuscle(
          dto.getMuscles().stream()
              .filter(muscle -> muscle.getRole().equals("PRIMARY"))
              .findFirst()
              .map(ExerciseMuscleRequest::toMuscle)
              .orElse(null));
      Exercise savedExercise = exerciseRepository.save(ex);
      // Add muscles
      if (dto.getMuscles() != null && !dto.getMuscles().isEmpty()) {
        saveMusclesForExercise(savedExercise, dto.getMuscles());
      }

      if (dto.getEquipmentTypeCode() != null) {
        saveEquipmentForExercise(savedExercise, dto.getEquipmentTypeCode());
      }
      log.info("imported: {}", imported);
      imported++;
    }
    return imported;
  }

  @Override
  public List<MuscleResponse> getMuscles() {
    List<Muscle> muscles = muscleRepository.findAll();
    return muscles.stream().map(muscleMapper::mapToResponse).toList();
  }

  @Transactional
  @Override
  public void updateExercise(UUID exerciseId, UpdateExerciseRequest request) {
    redisService.deletePattern("exercise:*");
      Exercise exercise = exerciseRepository.findByIdAndIsDeletedFalse(exerciseId).orElseThrow(
        () -> new ResourceNotFoundException("Exercise", exerciseId.toString())
      );

      if (!request.getName().equals(exercise.getName())){
        exercise.setName(request.getName());
        String newSlug = generateSlug(request.getName());
        exercise.setSlug(newSlug);
      }

      if(!request.getSafetyNotes().equals(exercise.getSafetyNotes())){
        exercise.setSafetyNotes(request.getSafetyNotes());
      }

      if(!request.getBodyPart().equals(exercise.getBodyPart())){
        exercise.setBodyPart(request.getBodyPart());
      }

      if (!request.getInstructions().equals(exercise.getInstructions())){
        exercise.setInstructions(request.getInstructions());
      }
    exercise.setExerciseType(ExerciseType.valueOf(request.getExerciseType()));
  }

  @Transactional
  @Override
  public void deleteExercise(UUID exerciseId) {
    Exercise exercise = exerciseRepository.findByIdAndIsDeletedFalse(exerciseId).orElseThrow(
      () -> new ResourceNotFoundException("Exercise", exerciseId.toString()));
    exercise.markAsDeleted();
    redisService.deletePattern("exercise:*");
  }

  private Integer difficultyLevelForExercise(String equipmentCode) {

    switch (equipmentCode) {
      case "body_weight":
      case "assisted":
      case "band":
      case "resistance_band":
        return 1;

      case "dumbbell":
      case "kettlebell":
      case "medicine_ball":
      case "stability_ball":
      case "bosu_ball":
      case "stationary_bike":
      case "elliptical_machine":
      case "roller":
      case "wheel_roller":
        return 2;

      case "barbell":
      case "ez_barbell":
      case "cable":
      case "rope":
      case "hammer":
      case "stepmill_machine":
      case "skierg_machine":
      case "upper_body_ergometer":
        return 3;

      case "olympic_barbell":
      case "trap_bar":
      case "smith_machine":
      case "leverage_machine":
      case "sled_machine":
      case "tire":
      case "weighted":
        return 4;
      default:
        return 2;
    }
  }

  private ExerciseListResponse mapToListResponse(Exercise exercise) {
    // Get secondary muscles
    List<ExerciseMuscle> muscles =
        exerciseMuscleRepository.findByExerciseIdOrderByRole(exercise.getId());

    List<String> primaryMuscles =
        muscles.stream()
            .filter(em -> "PRIMARY".equals(em.getRole()))
            .map(em -> em.getMuscle().getName())
            .toList();

    List<String> secondaryMuscles =
        muscles.stream()
            .filter(em -> "SECONDARY".equals(em.getRole()))
            .map(em -> em.getMuscle().getName())
            .toList();

    return ExerciseListResponse.builder()
        .id(exercise.getId())
        .slug(exercise.getSlug())
        .name(exercise.getName())
        .level(null)
        .primaryMuscle(primaryMuscles)
        .equipment(
            exercise.getEquipment() != null
                ? exerciseMapper.toEquipmentTypeResponse(exercise.getEquipment()).getName()
                : null)
        .instructions(Arrays.stream(exercise.getInstructions().split(",")).toList())
        .safetyNotes(exercise.getSafetyNotes())
        .thumbnailUrl(exercise.getThumbnailUrl())
        .exerciseCategory(exercise.getExerciseCategory().getName())
        .exerciseType(exercise.getExerciseType().toString())
        .bodyPart(exercise.getBodyPart())
        .secondaryMuscles(secondaryMuscles)
        .createdAt(exercise.getCreatedAt())
        .updatedAt(exercise.getUpdatedAt())
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
            .toList();

    return ExerciseDetailResponse.builder()
        .id(exercise.getId())
        .slug(exercise.getSlug())
        .name(exercise.getName())
        .level(null)
        .primaryMuscle(
            muscleResponses.stream()
                .filter(e -> e.getRole().equalsIgnoreCase("primary"))
                .map(em -> em.getMuscleName())
                .toList())
        .equipment(
            exercise.getEquipment() != null
                ? exerciseMapper.toEquipmentTypeResponse(exercise.getEquipment()).getName()
                : null)
        .instructions(Arrays.stream(exercise.getInstructions().split(",")).toList())
        .safetyNotes(exercise.getSafetyNotes())
        .thumbnailUrl(exercise.getThumbnailUrl())
        .exerciseCategory(exercise.getExerciseCategory().getName())
        .exerciseType(exercise.getExerciseType().toString())
        .bodyPart(exercise.getBodyPart())
        .secondaryMuscles(
            muscleResponses.stream()
                .filter(e -> e.getRole().equalsIgnoreCase("secondary"))
                .map(em -> em.getMuscleName())
                .toList())
        .bodyPart(exercise.getBodyPart())
        .createdAt(exercise.getCreatedAt())
        .updatedAt(exercise.getUpdatedAt())
        .createdBy(exercise.getCreatedBy())
        .updatedBy(exercise.getUpdatedBy())
        .build();
  }

  private Specification<Exercise> buildSearchSpecification(ExerciseSearchRequest request) {
    return ExerciseSpecification.hasKeyword(request.getKeyword())
        .and(ExerciseSpecification.hasLevel(request.getLevel()))
        .and(ExerciseSpecification.hasPrimaryMuscle(request.getPrimaryMuscle()))
        .and(ExerciseSpecification.hasEquipment(request.getEquipmentType()))
        .and(ExerciseSpecification.hasMuscles(request.getMusclesCodes()))
        .and(ExerciseSpecification.hasExerciseType(request.getExerciseType()));
  }

  // Private helper methods
  private void saveMusclesForExercise(
      Exercise exercise, List<ExerciseMuscleRequest> muscleRequests) {
    // Validate all muscles exist
    List<String> muscleCodes =
        muscleRequests.stream()
            .map(ExerciseMuscleRequest::getMuscleCode)
          .toList();

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
  // Private helper methods
  private void saveEquipmentForExercise(Exercise exercise, String exerciseEquipment) {

    Equipment equipment =
        equipmentRepository
            .findById(exerciseEquipment)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

    ExerciseEquipment exerciseEquipmentEntity = new ExerciseEquipment();

    ExerciseEquipmentId id = new ExerciseEquipmentId();
    id.setExerciseId(exercise.getId());
    id.setEquipmentTypeCode(equipment.getCode());

    exerciseEquipmentEntity.setId(id);
    exerciseEquipmentEntity.setExercise(exercise);
    exerciseEquipmentEntity.setEquipment(equipment);

    exerciseEquipmentRepository.save(exerciseEquipmentEntity);
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

  private Equipment getOrCreateEquipment(String equipmentCode) {
    return equipmentRepository
        .findById(equipmentCode)
        .orElseGet(
            () -> {
              Equipment newEquipment = new Equipment();
              newEquipment.setCode(equipmentCode.toLowerCase());
              newEquipment.setName(StringUtils.capitalize(equipmentCode.toLowerCase()));
              return equipmentRepository.save(newEquipment);
            });
  }

  private String generateSlug(String name) {
    return name.trim().toLowerCase().replace(" ", "-");
  }
}
