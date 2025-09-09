package com.thentrees.gymhealthtech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.common.ExerciseLevel;
import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
import com.thentrees.gymhealthtech.util.ExtractValidationErrors;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${app.prefix}/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

  private final ExerciseLibraryService exerciseLibraryService;
  private final ExtractValidationErrors extractValidationErrors;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<APIResponse<PagedResponse<ExerciseListResponse>>> getExercises(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) ExerciseLevel level,
      @RequestParam(required = false) String primaryMuscle,
      @RequestParam(required = false) List<String> musclesCodes,
      @RequestParam(required = false) String equipmentType,
      @RequestParam(required = false) String exerciseType,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    ExerciseSearchRequest request =
        ExerciseSearchRequest.builder()
            .keyword(keyword)
            .level(level)
            .primaryMuscle(primaryMuscle)
            .musclesCodes(musclesCodes)
            .equipmentType(equipmentType)
            .exerciseType(exerciseType)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

    PagedResponse<ExerciseListResponse> exercises = exerciseLibraryService.getExercises(request);
    return ResponseEntity.ok(APIResponse.success(exercises));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<ExerciseDetailResponse>> createExercise(
      @Valid @RequestBody CreateExerciseRequest request,
      BindingResult bindingResult,
      Authentication auth) {
    if (bindingResult.hasErrors()) {
      Map<String, String> errors = extractValidationErrors.extract(bindingResult);
      ApiError apiError =
          ApiError.builder()
              .code("VALIDATION_ERROR")
              .fieldErrors(
                  errors.entrySet().stream()
                      .map(
                          entry ->
                              FieldError.builder()
                                  .field(entry.getKey())
                                  .message(entry.getValue())
                                  .build())
                      .toList())
              .build();
      return ResponseEntity.badRequest()
          .body(
              APIResponse.error(
                  "Validation failed", objectMapper.convertValue(apiError, ApiError.class)));
    }
    ExerciseDetailResponse exercise = exerciseLibraryService.createExercise(request, auth);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(exercise));
  }

//   import exercise from file json
  @PostMapping("/import-exercise")
  @PreAuthorize("hasRole('ADMIN')")
  public String importJson(@RequestParam("file") MultipartFile file) throws Exception {
    int count = exerciseLibraryService.importExercisesFromJson(file);
    return "Imported/Updated " + count + " exercises.";
  }
}
