package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateExerciseRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.ExerciseLevel;
import com.thentrees.gymhealthtech.service.ExerciseLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AppConstants.API_V1 + "/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

  private final ExerciseLibraryService exerciseLibraryService;

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
  public ResponseEntity<APIResponse<PagedResponse<ExerciseListResponse>>> exercises(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) ExerciseLevel level,
      @RequestParam(required = false) String primaryMuscle,
      @RequestParam(required = false) List<String> musclesCodes,
      @RequestParam(required = false) String equipmentType,
      @RequestParam(required = false) String exerciseType,
      @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
      @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDirection) {
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
  public ResponseEntity<APIResponse<ExerciseDetailResponse>> exercise(
      @Valid @RequestBody CreateExerciseRequest request) {
    ExerciseDetailResponse exercise = exerciseLibraryService.createExercise(request);
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
  public ResponseEntity<APIResponse<ExerciseDetailResponse>> exerciseById(
      @PathVariable("id") UUID id) {
    ExerciseDetailResponse exerciseDetailResponse =
        exerciseLibraryService.getExerciseById(id);
    return ResponseEntity.ok(APIResponse.success(exerciseDetailResponse));
  }

  @Operation(
      method = "POST",
      description = "import file json include exercises",
      summary = "endpoint for ADMIN import list exercise into database")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "import exercises from file json"),
        @ApiResponse(responseCode = "403", description = "Unauthorize - AccessDenied"),
        @ApiResponse(responseCode = "500", description = "internal server error")
      })
  @PostMapping("/import-exercise")
  @PreAuthorize("hasRole('ADMIN')")
  public String importJson(@RequestParam("file") MultipartFile file) throws Exception {
    int count = exerciseLibraryService.importExercisesFromJson(file);
    return "Imported/Updated " + count + " exercises.";
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> updateExercise(
    @PathVariable("id") UUID exerciseId,
    @Valid @RequestBody UpdateExerciseRequest request
  ){
    exerciseLibraryService.updateExercise(exerciseId, request);
    return ResponseEntity.ok(
      APIResponse.success(SuccessMessages.UPDATE_EXERCISE_SUCCESS)
    );
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> deleteExercise(
    @PathVariable("id") UUID exerciseId
  ){
    exerciseLibraryService.deleteExercise(exerciseId);
    return ResponseEntity.ok(
      APIResponse.success(SuccessMessages.DEL_EXERCISE_SUCCESS)
    );
  }
  @Operation(method = "GET", description = "Get all muscle", summary = "get muscles")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "response collection muscle",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MuscleResponse.class),
                    examples =
                        @ExampleObject(
                            """
              {
                  "status": "success",
                  "data": [
                      {
                          "code": "shins",
                          "name": "Shins"
                      },
                      {
                          "code": "hands",
                          "name": "Hands"
                      },
                  ],
                  "timestamp": "2025-09-11T12:29:20.7812718"
              }
              """))),
        @ApiResponse(responseCode = "500", description = "internal server error")
      })
  @GetMapping("/muscles")
  public ResponseEntity<APIResponse<List<MuscleResponse>>> getMuscles() {
    return ResponseEntity.ok(APIResponse.success(exerciseLibraryService.getMuscles()));
  }
}
