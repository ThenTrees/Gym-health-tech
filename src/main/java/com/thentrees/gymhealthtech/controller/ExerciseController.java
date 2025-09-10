package com.thentrees.gymhealthtech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.common.ExerciseLevel;
import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
import com.thentrees.gymhealthtech.util.ExtractValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  @Operation(
      method = "GET",
      summary = "Retrieve all exercises with optional fuzzy search filtering",
      description = "Get all exercises with optional search")
  @ApiResponses(
      value = {
        @ApiResponse(
            description = "Retrieve all exercises with optional fuzzy search filtering",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExerciseListResponse.class),
                    examples =
                        @ExampleObject(
                            """
            {
                "status": "success",
                "data": {
                    "content": [],
                    "pagination": {
                        "page": 0,
                        "size": 20,
                        "totalElements": 0,
                        "totalPages": 0,
                        "hasNext": false,
                        "hasPrevious": false,
                        "sortBy": null,
                        "sortDirection": null
                    }
                },
                "timestamp": "2025-09-10T17:03:46.1770877"
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
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

  @GetMapping("/{id}")
  @Operation(
      method = "GET",
      summary = "get info detail of exercise by id",
      description = "get info detail of exercise by id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "response detail info exercise",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExerciseDetailResponse.class),
                    examples =
                        @ExampleObject(
                            """
            {
            "id": "17180a84-4992-4dab-9056-4112505c650a",
                            "slug": "neck-side-stretch",
                            "name": "neck side stretch",
                            "level": null,
                            "primaryMuscle": [
                                "Levator Scapulae"
                            ],
                            "equipment": "Body Weight",
                            "thumbnailUrl": "https://static.exercisedb.dev/media/x2chWLO.gif",
                            "instructions": [
                                "[Step:1 Stand or sit up straight with your shoulders relaxed.",
                                " Step:2 Tilt your head to one side",
                                " bringing your ear towards your shoulder.",
                                " Step:3 Hold the stretch for 15-30 seconds.",
                                " Step:4 Repeat on the other side.",
                                " Step:5 Perform 2-4 sets on each side.]"
                            ],
                            "safetyNotes": null,
                            "exerciseCategory": "Stretching",
                            "exerciseType": "STRETCHING",
                            "bodyPart": "[neck]",
                            "secondaryMuscles": [
                                "Sternocleidomastoid",
                                "Trapezius"
                            ],
                            "createdAt": "2025-09-10T22:29:38.259674",
                            "updatedAt": "2025-09-10T22:29:38.259674"
            }
            """))),
        @ApiResponse(responseCode = "404", description = "exercise with id not found")
      })
  public ResponseEntity<APIResponse<ExerciseDetailResponse>> getExerciseDetail(
      @PathVariable("id") String id) {
    log.info("Get exercise by id: {}", id);
    ExerciseDetailResponse exerciseDetailResponse =
        exerciseLibraryService.getExerciseById(UUID.fromString(id));
    return ResponseEntity.ok(APIResponse.success(exerciseDetailResponse));
  }

  @Operation(
      method = "POST",
      description = "import file json include exercises",
      summary = "endpoint for ADMIN import list exercise into database")
  @PostMapping("/import-exercise")
  //  @PreAuthorize("hasRole('ADMIN')")
  public String importJson(@RequestParam("file") MultipartFile file) throws Exception {
    int count = exerciseLibraryService.importExercisesFromJson(file);
    return "Imported/Updated " + count + " exercises.";
  }
}
