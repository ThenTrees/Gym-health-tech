package com.thentrees.gymhealthtech.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.thentrees.gymhealthtech.dto.response.SessionResponse;
import com.thentrees.gymhealthtech.dto.response.SessionSetResponse;
import com.thentrees.gymhealthtech.model.Session;
import com.thentrees.gymhealthtech.model.SessionSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PlanMapper.class})
public interface SessionMapper {

  @Mapping(target = "planDayId", expression = "java(session.getPlanDay() != null ? session.getPlanDay().getId() : null)")
  @Mapping(target = "planDayName", expression = "java(session.getPlanDay() != null ? session.getPlanDay().getSplitName() : null)")
  @Mapping(target = "durationMinutes", ignore = true)
  @Mapping(target = "totalSets", ignore = true)
  @Mapping(target = "completedSets", ignore = true)
  @Mapping(target = "completionPercentage", ignore = true)
  @Mapping(target = "totalVolume", ignore = true)
  @Mapping(target = "sessionSets", ignore = true)
  @Mapping(target = "plannedItems", ignore = true)
  SessionResponse toResponse(Session session);

  default SessionResponse toResponse(Session session, boolean includeSessionSets) {
    SessionResponse response = toResponse(session);
    
    // Calculate duration
    if (session.getEndedAt() != null && session.getStartedAt() != null) {
      long minutes = Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes();
      response.setDurationMinutes((int) minutes);
    }
    
    if (includeSessionSets && session.getSessionSets() != null) {
      List<SessionSetResponse> sessionSetDtos = toSessionSetResponseList(session.getSessionSets());
      response.setSessionSets(sessionSetDtos);
      
      // Calculate summary stats
      response.setTotalSets(sessionSetDtos.size());
      response.setCompletedSets(
          (int) sessionSetDtos.stream()
              .filter(SessionSetResponse::getIsCompleted)
              .count());
      response.setCompletionPercentage(
          response.getTotalSets() > 0
              ? (double) response.getCompletedSets() / response.getTotalSets() * 100
              : 0.0);
      response.setTotalVolume(
          sessionSetDtos.stream()
              .mapToInt(s -> s.getVolume() != null ? s.getVolume() : 0)
              .sum());
    }
    
    return response;
  }

  @Mapping(target = "sessionId", expression = "java(sessionSet.getSession() != null ? sessionSet.getSession().getId() : null)")
  @Mapping(target = "exerciseId", expression = "java(sessionSet.getExercise() != null ? sessionSet.getExercise().getId() : null)")
  @Mapping(target = "exerciseName", expression = "java(sessionSet.getExercise() != null ? sessionSet.getExercise().getName() : null)")
  @Mapping(target = "isCompleted", ignore = true)
  @Mapping(target = "isSkipped", ignore = true)
  @Mapping(target = "completedAt", ignore = true)
  @Mapping(target = "performanceComparison", ignore = true)
  @Mapping(target = "volume", ignore = true)
  SessionSetResponse toSessionSetResponse(SessionSet sessionSet);

  default SessionSetResponse toSessionSetResponseWithComputed(SessionSet sessionSet) {
    SessionSetResponse response = toSessionSetResponse(sessionSet);
    
    // Determine completion status
    JsonNode actual = sessionSet.getActual();
    if (actual != null) {
      response.setIsSkipped(actual.has("isSkipped") && actual.get("isSkipped").asBoolean());
      response.setIsCompleted(actual.has("completedAt") && !response.getIsSkipped());
      
      if (response.getIsCompleted() && actual.has("completedAt")) {
        try {
          response.setCompletedAt(LocalDateTime.parse(actual.get("completedAt").asText()));
        } catch (Exception e) {
          // Ignore parsing errors
        }
      }
      
      // Calculate volume
      if (actual.has("reps") && actual.has("weight")) {
        try {
          int reps = actual.get("reps").asInt();
          double weight = actual.get("weight").asDouble();
          response.setVolume((int) (reps * weight));
        } catch (Exception e) {
          // Ignore calculation errors
        }
      }
      
      // Performance comparison
      response.setPerformanceComparison(
          calculatePerformanceComparison(sessionSet.getPlanned(), actual));
    }
    
    return response;
  }

  default List<SessionSetResponse> toSessionSetResponseList(List<SessionSet> sessionSets) {
    if (sessionSets == null) {
      return Collections.emptyList();
    }
    return sessionSets.stream()
        .map(this::toSessionSetResponseWithComputed)
        .toList();
  }

  default String calculatePerformanceComparison(JsonNode planned, JsonNode actual) {
    if (planned == null || actual == null) {
      return "unknown";
    }
    
    if (!planned.has("reps") || !actual.has("reps")) {
      return "unknown";
    }
    
    try {
      // Handle rep ranges like "8-12"
      String plannedReps = planned.get("reps").asText();
      int actualReps = actual.get("reps").asInt();
      
      if (plannedReps.contains("-")) {
        String[] range = plannedReps.split("-");
        int minReps = Integer.parseInt(range[0]);
        int maxReps = Integer.parseInt(range[1]);
        
        if (actualReps > maxReps) {
          return "above_plan";
        } else if (actualReps < minReps) {
          return "below_plan";
        } else {
          return "as_planned";
        }
      } else {
        int plannedRepsInt = Integer.parseInt(plannedReps);
        if (actualReps > plannedRepsInt) {
          return "above_plan";
        } else if (actualReps < plannedRepsInt) {
          return "below_plan";
        } else {
          return "as_planned";
        }
      }
    } catch (Exception e) {
      return "unknown";
    }
  }
}

