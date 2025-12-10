package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.model.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PlanMapper {

  @Named("localDateTimeToOffsetDateTime")
  default OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atOffset(ZoneOffset.UTC);
  }

  @Mapping(target = "totalDays", expression = "java(calculateTotalDays(plan))")
  @Mapping(target = "totalExercises", expression = "java(calculateTotalExercises(plan))")
  @Mapping(target = "goal", expression = "java(plan.getGoal() != null ? toGoalResponse(plan.getGoal()) : null)")
  @Mapping(target = "planDays", ignore = true)
  @Mapping(target = "completedSessions", ignore = true)
  @Mapping(target = "progressPercentage", ignore = true)
  @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToOffsetDateTime")
  @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToOffsetDateTime")
  PlanResponse toResponse(Plan plan);

  default PlanResponse toResponseWithDetails(Plan plan, Integer completedSessions, boolean includePlanDays) {
    PlanResponse response = toResponse(plan);
    if (completedSessions != null) {
      response.setCompletedSessions(completedSessions);

      if (response.getTotalDays() != null && response.getTotalDays() > 0) {
        response.setProgressPercentage((double) completedSessions / response.getTotalDays() * 100);
      }
    }

    if (includePlanDays && plan.getPlanDays() != null) {
      response.setPlanDays(toPlanDayResponseList(plan.getPlanDays(), true));
    }

    return response;
  }

  default PlanResponse toResponseWithFullDetails(Plan plan, Integer completedSessions,
      java.util.function.Function<PlanDay, Integer> estimatedDurationCalculator,
      java.util.function.Function<PlanDay, Boolean> isCompletedChecker,
      java.util.function.Function<PlanItem, Integer> timesCompletedCalculator,
      java.util.function.Function<PlanItem, ExerciseDetailResponse> exerciseMapper) {
    PlanResponse response = toResponse(plan);
    if (completedSessions != null) {
      response.setCompletedSessions(completedSessions);

      if (response.getTotalDays() != null && response.getTotalDays() > 0) {
        response.setProgressPercentage((double) completedSessions / response.getTotalDays() * 100);
      }
    }

    if (plan.getPlanDays() != null) {
      response.setPlanDays(plan.getPlanDays().stream()
          .map(day -> {
            Integer estimatedMinutes = estimatedDurationCalculator != null
                ? estimatedDurationCalculator.apply(day) : null;
            Boolean isCompleted = isCompletedChecker != null
                ? isCompletedChecker.apply(day) : null;

            PlanDayResponse dayResponse = toPlanDayResponse(day, true, estimatedMinutes, isCompleted);

            // Map plan items with exercise and timesCompleted
            if (day.getPlanItems() != null && exerciseMapper != null && timesCompletedCalculator != null) {
              dayResponse.setPlanItems(day.getPlanItems().stream()
                  .map(item -> toPlanItemResponseWithDetails(
                      item,
                      exerciseMapper.apply(item),
                      timesCompletedCalculator.apply(item)))
                  .toList());
            } else if (day.getPlanItems() != null) {
              dayResponse.setPlanItems(toPlanItemResponseList(day.getPlanItems()));
            }

            return dayResponse;
          })
          .toList());
    }

    return response;
  }

  @Mapping(target = "totalDays", expression = "java(calculateTotalDays(plan))")
  @Mapping(target = "totalExercises", expression = "java(calculateTotalExercises(plan))")
  @Mapping(target = "id", expression = "java(plan.getId().toString())")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "goalName", expression = "java(plan.getGoal() != null && plan.getGoal().getObjective() != null ? plan.getGoal().getObjective().name() : null)")
  PlanSummaryResponse toSummaryResponse(Plan plan);

  @Mapping(target = "totalExercises", expression = "java(planDay.getPlanItems() != null ? planDay.getPlanItems().size() : 0)")
  @Mapping(target = "estimatedDurationMinutes", ignore = true)
  @Mapping(target = "isCompleted", ignore = true)
  @Mapping(target = "lastCompletedDate", ignore = true)
  @Mapping(target = "planItems", ignore = true)
  PlanDayResponse toPlanDayResponse(PlanDay planDay);

  default PlanDayResponse toPlanDayResponse(PlanDay planDay, boolean includeItems,
      Integer estimatedDurationMinutes, Boolean isCompleted) {
    PlanDayResponse response = toPlanDayResponse(planDay);

    if (planDay.getPlanItems() != null) {
      response.setTotalExercises(planDay.getPlanItems().size());

      if (estimatedDurationMinutes != null) {
        response.setEstimatedDurationMinutes(estimatedDurationMinutes);
      }

      if (isCompleted != null) {
        response.setIsCompleted(isCompleted);
      }

      if (includeItems) {
        response.setPlanItems(toPlanItemResponseList(planDay.getPlanItems()));
      }
    }

    return response;
  }

  default List<PlanDayResponse> toPlanDayResponseList(List<PlanDay> planDays, boolean includeItems) {
    if (planDays == null) {
      return Collections.emptyList();
    }
    return planDays.stream()
        .map(day -> toPlanDayResponse(day, includeItems, null, null))
        .toList();
  }

  default PlanDayResponse toPlanDayResponse(PlanDay planDay, boolean includeItems) {
    return toPlanDayResponse(planDay, includeItems, null, null);
  }

  @Mapping(target = "exercise", source = "exercise")
  @Mapping(target = "timesCompleted", ignore = true)
  @Mapping(target = "lastPerformance", ignore = true)
  @Mapping(target = "progressTrend", ignore = true)
  PlanItemResponse toPlanItemResponse(PlanItem planItem);

  default ExerciseDetailResponse toExerciseDetailResponse(Exercise exercise){
    if (exercise == null) {
      return null;
    }

    return ExerciseDetailResponse.builder()
      .id(exercise.getId())
      .name(exercise.getName())
      .slug(exercise.getSlug())
      .primaryMuscle(exercise.getPrimaryMuscle().getName())
      .equipment(exercise.getEquipment().getName())
      .instructions(exercise.getInstructions())
      .safetyNotes(exercise.getSafetyNotes())
      .thumbnailUrl(exercise.getThumbnailUrl())
      .exerciseCategory(exercise.getExerciseCategory() != null ? exercise.getExerciseCategory().getName() : null)
      .exerciseType(exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null)
      .bodyPart(exercise.getBodyPart())
      .secondaryMuscles(exercise.getExerciseMuscles().stream().map(em -> em.getMuscle().getName()).toList())
      .createdAt(exercise.getCreatedAt())
      .updatedAt(exercise.getUpdatedAt())
      .createdBy(exercise.getCreatedBy())
      .updatedBy(exercise.getUpdatedBy())
        .build();
  }

  default PlanItemResponse toPlanItemResponseWithDetails(PlanItem planItem,
      ExerciseDetailResponse exercise, Integer timesCompleted) {
    PlanItemResponse response = toPlanItemResponse(planItem);
    if (exercise != null) {
      response.setExercise(exercise);
    }
    if (timesCompleted != null) {
      response.setTimesCompleted(timesCompleted);
    }
    return response;
  }

  default List<PlanItemResponse> toPlanItemResponseList(List<PlanItem> planItems) {
    if (planItems == null) {
      return Collections.emptyList();
    }
    return planItems.stream()
        .map(this::toPlanItemResponse)
        .toList();
  }

  default GoalResponse toGoalResponse(Goal goal) {
    if (goal == null) {
      return null;
    }
    return GoalResponse.builder()
        .id(goal.getId().toString())
        .userId(goal.getUser() != null ? goal.getUser().getId().toString() : null)
        .objective(goal.getObjective())
        .sessionsPerWeek(goal.getSessionsPerWeek())
        .sessionMinutes(goal.getSessionMinutes())
        .preferences(goal.getPreferences())
        .startedAt(goal.getStartedAt())
        .endedAt(goal.getEndedAt())
        .createdAt(goal.getCreatedAt())
        .status(goal.getStatus())
        .estimatedCaloriesPerSession(goal.getEstimatedCaloriesPerSession())
        .difficultyAssessment(goal.getDifficultyAssessment())
        .recommendedEquipment(goal.getRecommendedEquipment())
        .healthSafetyNotes(goal.getHealthSafetyNotes())
        .build();
  }

  default Integer calculateTotalDays(Plan plan) {
    if (plan == null || plan.getPlanDays() == null) {
      return 0;
    }
    return plan.getPlanDays().size();
  }

  default Integer calculateTotalExercises(Plan plan) {
    if (plan == null || plan.getPlanDays() == null) {
      return 0;
    }
    return plan.getPlanDays().stream()
        .mapToInt(day -> day.getPlanItems() != null ? day.getPlanItems().size() : 0)
        .sum();
  }
}

