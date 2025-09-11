package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateGoalRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.GoalResponse;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.service.GoalService;
import com.thentrees.gymhealthtech.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/goals")
@Validated
@Slf4j
@RequiredArgsConstructor
public class GoalController {
  private final UserService userService;
  private final GoalService goalService;

  @PostMapping
  public ResponseEntity<APIResponse<GoalResponse>> createGoal(
      @PathVariable String userId, @Valid @RequestBody CreateGoalRequest request) {
    // Validate user exists
    User user = userService.getUserById(UUID.fromString(userId));

    APIResponse<GoalResponse> response = APIResponse.success(goalService.createGoal(user, request));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<APIResponse<List<GoalResponse>>> getUserGoals(
      @PathVariable String userId, @RequestParam(defaultValue = "false") boolean includeCompleted) {

    try {
      List<GoalResponse> goals = goalService.getUserGoals(userId, includeCompleted);
      return ResponseEntity.ok(APIResponse.success(goals));

    } catch (Exception e) {
      log.error("Error retrieving goals for user: {}", userId, e);
      return ResponseEntity.ok(APIResponse.error("Error retrieving goals for user: {}", userId));
    }
  }

  @Operation(method = "GET", description = "Get goal active")
  @ApiResponse(
      responseCode = "200",
      description = "response goal active",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GoalResponse.class),
              examples =
                  @ExampleObject(
                      """
          {
                      "id": "b7c1bc86-d2c2-479a-b062-51858686992e",
                      "userId": "ed046514-3530-44eb-ae3e-713ced29f105",
                      "objective": "LOSE_FAT",
                      "sessionsPerWeek": 3,
                      "sessionMinutes": 120,
                      "preferences": {
                          "timelineWeeks": 4,
                          "currentWeightKg": 60.0,
                          "targetWeightLossKg": 3.0
                      },
                      "startedAt": "2025-09-11T16:43:55.732976",
                      "endedAt": null,
                      "createdAt": "2025-09-11T16:43:55.743447",
                      "status": "ACTIVE",
                      "estimatedCaloriesPerSession": null,
                      "difficultyAssessment": null,
                      "recommendedEquipment": null,
                      "healthSafetyNotes": null
                  }
          """)))
  @GetMapping("/active")
  public ResponseEntity<APIResponse<GoalResponse>> getActiveGoal(@PathVariable String userId) {
    try {
      GoalResponse activeGoal = goalService.getActiveGoal(userId);

      return ResponseEntity.ok(APIResponse.success(activeGoal));

    } catch (Exception e) {
      log.error("Error retrieving active goal for user: {}", userId, e);
      return ResponseEntity.ok(
          APIResponse.error("Error retrieving active goal for user: {}", userId));
    }
  }
}
