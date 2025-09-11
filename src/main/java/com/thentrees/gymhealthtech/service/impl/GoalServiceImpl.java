package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.common.GoalStatus;
import com.thentrees.gymhealthtech.common.ObjectiveType;
import com.thentrees.gymhealthtech.dto.request.CreateGoalRequest;
import com.thentrees.gymhealthtech.dto.response.GoalResponse;
import com.thentrees.gymhealthtech.event.GoalCreatedEvent;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.DataProcessingException;
import com.thentrees.gymhealthtech.model.Goal;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.repository.GoalRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.GoalService;
import com.thentrees.gymhealthtech.service.UserProfileService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoalServiceImpl implements GoalService {

  private final GoalRepository goalRepository;
  private final UserProfileService userProfileService;
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final UserRepository userRepository;

  @Override
  public GoalResponse createGoal(User user, CreateGoalRequest request) {

    log.info("Creating goal for user: {}, objective: {}", user.getId(), request.getObjective());

    log.info("request data: {}", request);

    UUID userId = user.getId();

    UserProfile profile = userProfileService.getUserProfileById(userId);
    // Validate goal preferences
    if (request.getPreferences() != null) {
      request.getPreferences().validate(request.getObjective());
    }

    // 3. Perform health and safety checks
    performHealthSafetyChecks(profile, request);

    // 4. End any existing active goals
    endActiveGoals(userId);
    // 5. Create and save new goal
    Goal goal = createGoalEntity(user, request, profile);
    Goal savedGoal = goalRepository.save(goal);

    // 6. Publish goal created event for AI plan generation
    eventPublisher.publishEvent(new GoalCreatedEvent(savedGoal, profile));

    // 7. Build and return response
    GoalResponse response = GoalResponse.from(savedGoal);
    enhanceResponse(response, request, profile);

    log.info("Goal created successfully: {}", savedGoal.getId());
    return response;
  }

  @Override
  public List<GoalResponse> getUserGoals(String userId, boolean includeCompleted) {

    User user = userRepository.findById(UUID.fromString(userId)).get();

    List<Goal> goals;
    if (includeCompleted) {
      goals = goalRepository.findAllByUserIdIncludingCompleted(user.getId());
    } else {
      goals = goalRepository.findAllByUserIdExcludingCompleted(user.getId(), GoalStatus.COMPLETED);
    }

    return goals.stream().map(goal -> GoalResponse.from(goal)).toList();
  }

  @Override
  public GoalResponse getActiveGoal(String userId) {
    log.info("Fetching active goal for user: {}", userId);

    Goal activeGoal =
        goalRepository
            .findActiveGoalByUserId(UUID.fromString(userId), GoalStatus.ACTIVE)
            .orElse(null);
    return GoalResponse.from(activeGoal);
  }

  /**
   * check age of user to warning safety health
   *
   * @param profile
   * @param request
   */
  private void performHealthSafetyChecks(UserProfile profile, CreateGoalRequest request) {
    // Age-based checks
    if (profile.getAge() != null) {
      if (profile.getAge() < 16) {
        throw new BusinessException(
            "Users under 16 require parental supervision and medical clearance");
      }
      if (profile.getAge() > 65 && request.getSessionMinutes() > 60) {
        log.warn(
            "User over 65 requesting sessions longer than 60 minutes, recommending medical clearance");
      }
    }

    // BMI-based checks for weight loss goals
    if (request.getObjective() == ObjectiveType.LOSE_FAT && profile.getBmi() != null) {
      if (profile.getBmi().doubleValue() < 18.5) {
        throw new BusinessException(
            "BMI indicates underweight. Weight loss goals not recommended.");
      }
      if (profile.getBmi().doubleValue() > 35) {
        log.warn("BMI indicates obesity class II+. Medical supervision recommended.");
      }
    }

    // Pregnancy check for certain objectives || warning for woman pregnancy
    if (request.getPreferences() != null
        && request.getPreferences().getHealthConditions() != null
        && request.getPreferences().getHealthConditions().contains("pregnancy")) {

      if (request.getObjective() == ObjectiveType.LOSE_FAT) {
        throw new BusinessException("Weight loss goals not recommended during pregnancy");
      }
      if (request.getSessionMinutes() > 45) {
        log.warn("Extended exercise sessions during pregnancy require medical clearance");
      }
    }

    // Intensity checks for beginners => beginner do not
    if (request.getPreferences() != null
        && "beginner".equals(request.getPreferences().getExperienceLevel())
        && request.getSessionsPerWeek() > 4) {
      log.warn("High frequency training for beginners may increase injury risk");
    }
  }

  private void endActiveGoals(UUID userId) {
    List<Goal> activeGoals = goalRepository.findByUserIdAndEndedAtIsNull(userId);
    if (!activeGoals.isEmpty()) {
      log.info("Ending {} active goals for user: {}", activeGoals.size(), userId);
      activeGoals.forEach(
          goal -> {
            goal.setEndedAt(LocalDate.now());
            goal.setUpdatedAt(LocalDateTime.now());
          });
      goalRepository.saveAll(activeGoals);
    }
  }

  private Goal createGoalEntity(User user, CreateGoalRequest request, UserProfile profile) {
    Goal goal = new Goal();
    goal.setUser(user);
    goal.setObjective(request.getObjective());
    goal.setSessionsPerWeek(request.getSessionsPerWeek());
    goal.setSessionMinutes(request.getSessionMinutes());
    goal.setStartedAt(
        request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now());
    goal.setStatus(GoalStatus.ACTIVE);
    // Convert preferences to JSONB
    if (request.getPreferences() != null) {
      try {
        JsonNode preferencesJson = objectMapper.valueToTree(request.getPreferences());
        goal.setPreferences(preferencesJson);
      } catch (Exception e) {
        throw new DataProcessingException("Failed to process goal preferences", e);
      }
    }

    return goal;
  }

  private void enhanceResponse(
      GoalResponse response, CreateGoalRequest request, UserProfile profile) {
    // Calculate estimated calories per session
    response.setEstimatedCaloriesPerSession(calculateEstimatedCalories(request, profile));

    // Assess difficulty
    response.setDifficultyAssessment(assessDifficulty(request, profile));

    // Recommend equipment
    response.setRecommendedEquipment(recommendEquipment(request.getObjective()));

    // Add health and safety notes
    response.setHealthSafetyNotes(generateSafetyNotes(request, profile));
  }

  private Integer calculateEstimatedCalories(CreateGoalRequest request, UserProfile profile) {
    if (profile.getWeightKg() == null) return null;

    double weight = profile.getWeightKg().doubleValue();
    int minutes = request.getSessionMinutes();

    // Rough calorie estimation based on objective and weight
    double caloriesPerMinute =
        switch (request.getObjective()) {
          case LOSE_FAT -> 8.5; // High intensity mixed training
          case GAIN_MUSCLE -> 6.0; // Strength training
          case ENDURANCE -> 10.0; // Cardio focus
          case MAINTAIN -> 7.0; // Balanced approach
        };

    // Adjust for body weight (calories scale with weight)
    double adjustedCalories = caloriesPerMinute * (weight / 70.0); // 70kg baseline

    return (int) Math.round(adjustedCalories * minutes);
  }

  private String assessDifficulty(CreateGoalRequest request, UserProfile profile) {
    int difficultyScore = 0;

    // Frequency factor
    if (request.getSessionsPerWeek() >= 5) difficultyScore += 2;
    else if (request.getSessionsPerWeek() >= 3) difficultyScore += 1;

    // Duration factor
    if (request.getSessionMinutes() >= 90) difficultyScore += 2;
    else if (request.getSessionMinutes() >= 60) difficultyScore += 1;

    // Objective factor
    difficultyScore +=
        switch (request.getObjective()) {
          case LOSE_FAT -> 2; // Requires discipline
          case GAIN_MUSCLE -> 2; // Requires consistency
          case ENDURANCE -> 3; // High volume training
          case MAINTAIN -> 1; // More flexible
        };

    // Experience level factor
    if (request.getPreferences() != null
        && "beginner".equals(request.getPreferences().getExperienceLevel())) {
      difficultyScore += 1;
    }

    return switch (difficultyScore) {
      case 0, 1, 2 -> "EASY";
      case 3, 4, 5 -> "MODERATE";
      case 6, 7 -> "CHALLENGING";
      default -> "VERY_CHALLENGING";
    };
  }

  private List<String> recommendEquipment(ObjectiveType objective) {
    return switch (objective) {
      case LOSE_FAT -> Arrays.asList("resistance_bands", "dumbbells", "yoga_mat", "jump_rope");
      case GAIN_MUSCLE -> Arrays.asList("dumbbells", "barbell", "bench", "pull_up_bar");
      case ENDURANCE -> Arrays.asList("running_shoes", "heart_rate_monitor", "yoga_mat");
      case MAINTAIN -> Arrays.asList("yoga_mat", "resistance_bands", "dumbbells");
    };
  }

  private String generateSafetyNotes(CreateGoalRequest request, UserProfile profile) {
    List<String> notes = new ArrayList<>();

    // Age-based notes
    if (profile.getAge() != null) {
      if (profile.getAge() > 50) {
        notes.add("Consider medical clearance before starting intensive exercise");
      }
      if (profile.getAge() < 18) {
        notes.add("Youth training should focus on movement quality over intensity");
      }
    }

    // Objective-specific safety notes
    switch (request.getObjective()) {
      case LOSE_FAT:
        notes.add("Aim for 0.5-1kg weight loss per week for sustainable results");
        notes.add("Maintain adequate nutrition to support energy levels");
        break;
      case GAIN_MUSCLE:
        notes.add("Progressive overload should be gradual to prevent injury");
        notes.add("Ensure adequate protein intake and rest for recovery");
        break;
      case ENDURANCE:
        notes.add("Increase training volume by no more than 10% per week");
        notes.add("Include rest days to prevent overtraining");
        break;
    }

    // General safety notes
    notes.add("Stop exercising if you experience pain, dizziness, or unusual discomfort");
    notes.add("Stay hydrated and listen to your body");

    return String.join(". ", notes) + ".";
  }
}
