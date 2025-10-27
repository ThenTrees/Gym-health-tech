package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.SessionSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.exception.ValidationException;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.PlanDayRepository;
import com.thentrees.gymhealthtech.repository.PlanItemRepository;
import com.thentrees.gymhealthtech.repository.SessionRepository;
import com.thentrees.gymhealthtech.repository.SessionSetRepository;
import com.thentrees.gymhealthtech.repository.spec.SessionSpecification;
import com.thentrees.gymhealthtech.service.CustomPlanService;
import com.thentrees.gymhealthtech.service.SessionManagementService;
import com.thentrees.gymhealthtech.service.UserService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagementServiceImpl implements SessionManagementService {

  private final UserService userService;
  private final PlanDayRepository planDayRepository;
  private final SessionRepository sessionRepository;
  private final PlanItemRepository planItemRepository;
  private final SessionSetRepository sessionSetRepository;
  private final ObjectMapper objectMapper;

  public SessionResponse getSessionDetails(UUID userId, UUID sessionId) {
    log.info("Getting session details for user: {} and session: {}", userId, sessionId);

    Session session =
        sessionRepository
            .findByIdAndUserIdWithSets(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId.toString()));

    return convertSessionToResponse(session, true);
  }

  /**
   * Starts a new workout session for the user based on the specified plan day. business logic: -
   * Check if user has an active session - Check if plan day exists and is associated with user -
   * Create session entity - Create session sets based on plan items - Return session details
   *
   * @param userId
   * @param request
   * @return SessionResponse
   */
  @Override
  @Transactional
  public SessionResponse startSession(UUID userId, CreateStartSessionRequest request) {
    log.info("Starting session for user {} with plan day {}", userId, request.getPlanDayId());

    User user = userService.getUserById(userId);

    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanUserId(request.getPlanDayId(), userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Plan day not found or not associated with user"));

    // Check if there is already an active session for the user
    Optional<Session> activeSession =
        sessionRepository.findByUserIdAndStatus(userId, SessionStatus.IN_PROGRESS);
    if (activeSession.isPresent()) {
      log.error("Session already active for user {} with plan day {}", userId, planDay.getId());
      throw new ValidationException(
          "You already have an active workout session. Complete it first.");
    }

    var zone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
    var start = java.time.LocalDate.now(zone).atStartOfDay();
    var end = start.plusDays(1);

    // Check if this plan day already has a completed session today
    boolean hasCompletedToday =
        sessionRepository.existsByPlanDayIdAndStatusAndEndedAtGreaterThanEqualAndEndedAtLessThan(
            request.getPlanDayId(), SessionStatus.COMPLETED, start, end);
    if (hasCompletedToday) {
      log.warn(
          "Plan day {} already completed today, but allowing new session", request.getPlanDayId());
    }

    Session session = createSessionEntity(user, planDay, request);
    Session sessionSaved = sessionRepository.save(session);
    // Create session sets based on plan items
    List<PlanItem> planItems =
        planItemRepository.findByPlanDay_IdOrderByItemIndexAsc(planDay.getId());

    List<SessionSet> sessionSets = new ArrayList<>();
    for (PlanItem planItem : planItems) {
      JsonNode prescription = planItem.getPrescription();
      int plannedSets = prescription.get("sets").asInt();

      // Create individual session sets for each planned set
      for (int setIndex = 1; setIndex <= plannedSets; setIndex++) {
        SessionSet sessionSet = new SessionSet();
        sessionSet.setSession(sessionSaved);
        sessionSet.setExercise(planItem.getExercise());
        sessionSet.setSetIndex(setIndex);
        sessionSet.setPlanned(prescription.deepCopy());
        sessionSet.setPlanItem(planItem);

        // Initialize actual with planned values (user can modify during workout)
        ObjectNode actualNode =
            objectMapper
                .createObjectNode()
                .put("restSeconds", prescription.path("restSeconds").asInt(0));
        if (prescription.has("weight")) {
          actualNode.put("weight", prescription.get("weight").asDouble());
        }
        sessionSet.setActual(actualNode);

        SessionSet sessionSetSaved = sessionSetRepository.save(sessionSet);

        sessionSets.add(sessionSetSaved);
      }
    }
    session.setSessionSets(sessionSets);

    log.info(
        "Successfully started session {} with {} sets for user {}",
        session.getId(),
        sessionSets.size(),
        userId);
    return convertSessionToResponse(session, true);
  }

  @Override
  public SessionResponse getActiveSession(UUID userId) {
    log.info("Getting active session for user: {}", userId);

    Session session =
        sessionRepository
            .findByUserIdAndStatus(userId, SessionStatus.IN_PROGRESS)
            .orElseThrow(() -> new ResourceNotFoundException("No active workout session found"));

    return convertSessionToResponse(session, true);
  }

  @Override
  @Transactional
  public SessionResponse completeSession(
      UUID userId, UUID sessionId, CompleteSessionRequest request) {
    log.info("Completing session {} for user {}", sessionId, userId);

    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

    if (session.getStatus() != SessionStatus.IN_PROGRESS) {
      throw new ValidationException("Session is not in progress");
    }

    // Update session
    session.setEndedAt(request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now());
    session.setStatus(SessionStatus.COMPLETED);
    session.setSessionRpe(request.getSessionRpe());

    // Combine notes
    String completedNotes = session.getNotes() != null ? session.getNotes() : "";
    if (request.getNotes() != null) {
      completedNotes +=
          (completedNotes.isEmpty() ? "" : "\n\n") + "Post-workout: " + request.getNotes();
    }
    if (request.getWorkoutFeeling() != null) {
      completedNotes +=
          (completedNotes.isEmpty() ? "" : "\n") + "Feeling: " + request.getWorkoutFeeling();
    }

    session.setNotes(completedNotes);
    session = sessionRepository.save(session);

    // Update plan progression (could trigger notifications, plan adjustments, etc.)
    updatePlanProgression(session);

    log.info("Successfully completed session: {}", sessionId);
    return convertSessionToResponse(session, true);
  }

  @Override
  @Transactional
  public SessionSetResponse updateSessionSet(
      UUID userId, UUID sessionSetId, UpdateSessionSetRequest request) {
    log.info("Updating session set {} for user {}", sessionSetId, userId);

    SessionSet sessionSet =
        sessionSetRepository
            .findByIdAndSessionUserId(sessionSetId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session set not found"));

    if (sessionSet.getSession().getStatus() != SessionStatus.IN_PROGRESS) {
      throw new ValidationException("Cannot update sets in a non-active session");
    }

    // Update actual performance
    ObjectNode actualNode = (ObjectNode) sessionSet.getActual();
    actualNode.put("reps", request.getActualReps());

    if (request.getActualWeight() != null) {
      actualNode.put("weight", request.getActualWeight());
    }
    if (request.getRpe() != null) {
      actualNode.put("rpe", request.getRpe());
    }
    if (request.getRestSeconds() != null) {
      actualNode.put("restSeconds", request.getRestSeconds());
    }
    if (request.getSetDurationSeconds() != null) {
      actualNode.put("setDurationSeconds", request.getSetDurationSeconds());
    }
    if (request.getIsSkipped()) {
      actualNode.put("isSkipped", true);
    } else {
      actualNode.put("completedAt", LocalDateTime.now().toString());
    }

    if (request.getNotes() != null) {
      actualNode.put("notes", request.getNotes());
    }

    sessionSet.setActual(actualNode);
    sessionSet = sessionSetRepository.save(sessionSet);

    log.info("Successfully updated session set: {}", sessionSetId);
    return convertSessionSetToResponse(sessionSet);
  }

  @Override
  @Transactional
  public void cancelSession(UUID userId, UUID sessionId, String reason) {
    log.info("Cancelling session {} for user {} - reason: {}", sessionId, userId, reason);

    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw new ValidationException("Cannot cancel a completed session");
    }

    session.setStatus(SessionStatus.CANCELLED);
    session.setEndedAt(LocalDateTime.now());

    String cancelNote = "Session cancelled at " + LocalDateTime.now();
    if (reason != null) {
      cancelNote += " - Reason: " + reason;
    }

    String currentNotes = session.getNotes() != null ? session.getNotes() : "";
    session.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : "\n") + cancelNote);

    sessionRepository.save(session);

    log.info("Successfully cancelled session: {}", sessionId);
  }

  @Override
  @Transactional
  public void pauseSession(UUID userId, UUID sessionId, String reason) {
    log.info("Pausing session {} for user {} - reason: {}", sessionId, userId, reason);

    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

    if (session.getStatus() != SessionStatus.IN_PROGRESS) {
      throw new ValidationException("Can only pause an active session");
    }

    session.setStatus(SessionStatus.PAUSED);

    String pauseNote = "Session paused at " + LocalDateTime.now();
    if (reason != null) {
      pauseNote += " - Reason: " + reason;
    }

    String currentNotes = session.getNotes() != null ? session.getNotes() : "";
    session.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : "\n") + pauseNote);

    sessionRepository.save(session);

    log.info("Successfully paused session: {}", sessionId);
  }

  @Transactional
  @Override
  public SessionResponse resumeSession(UUID userId, UUID sessionId) {
    log.info("Resuming session {} for user {}", sessionId, userId);
    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

    if (session.getStatus() != SessionStatus.PAUSED) {
      throw new ValidationException("Can only resume a paused session");
    }
    session.setStatus(SessionStatus.IN_PROGRESS);
    String resumeNote = "Session resumed at " + LocalDateTime.now();
    String currentNotes = session.getNotes() != null ? session.getNotes() : "";
    session.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : "\n") + resumeNote);
    session = sessionRepository.save(session);
    log.info("Successfully resumed session: {}", sessionId);
    return convertSessionToResponse(session, true);
  }

  @Override
  public PagedResponse<SessionResponse> getAllSessions(UUID userId, SessionSearchRequest request) {
    log.info("Getting all sessions for user: {}", userId);

    Specification<Session> spec = buildSessionSpecification(userId, request);

    Pageable pageable =
        PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.Direction.fromString(request.getSortOrder()),
            request.getSortBy());

    Page<Session> sessionPage = sessionRepository.findAll(spec, pageable);
    Page<SessionResponse> response =
        sessionPage.map(session -> convertSessionToResponse(session, false));
    return PagedResponse.of(response);
  }

  @Transactional(readOnly = true)
  @Override
  public WeeklySummaryResponse getSummaryWeekSessions(UUID userId) {
    log.info("Getting session week summary for user: {} and plan day", userId);

    LocalDate today = LocalDate.now();
    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    User user = userService.getUserById(userId);

    List<Session> sessions =
        sessionRepository.findByUserAndStartedAtBetween(
            user,
            startOfWeek.atStartOfDay(),
            endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1));

    if (sessions.isEmpty()) {
      return WeeklySummaryResponse.builder()
        .weekStart(startOfWeek)
        .weekEnd(endOfWeek)
        .totalSessions(0)
        .completedSessions(0)
        .totalSets(0)
        .completedSets(0)
        .avgCompletionPercentage(0)
        .totalVolume(0)
        .totalDurationMinutes(0)
        .dailySummaries(Collections.emptyList())
        .build();
    }

    List<SessionResponse> sessionResponses = new ArrayList<>();
    for (Session session : sessions) {
      sessionResponses.add(convertSessionToResponse(session, true));
    }

    int totalSessions = sessionResponses.size();
    int completedSessions = (int) sessionResponses.stream()
      .filter(d -> d.getStatus() == SessionStatus.COMPLETED)
      .count();

    int totalSets = sessionResponses.stream().mapToInt(SessionResponse::getTotalSets).sum();

    int completedSets = sessionResponses.stream().mapToInt(SessionResponse::getCompletedSets).sum();
    int totalVolume = sessionResponses.stream().mapToInt(SessionResponse::getTotalVolume).sum();
    int totalDuration = sessionResponses.stream().mapToInt(SessionResponse::getDurationMinutes).sum();
    double avgCompletion = sessionResponses.stream()
      .mapToDouble(SessionResponse::getCompletionPercentage)
      .average()
      .orElse(0);

    // Tìm ngày có volume cao nhất (để hiển thị "mostTrainedDayName")
    Optional<SessionResponse> mostTrained = sessionResponses.stream()
      .max(Comparator.comparingInt(SessionResponse::getTotalVolume));

    return WeeklySummaryResponse.builder()
      .weekStart(startOfWeek)
      .weekEnd(endOfWeek)
      .totalSessions(totalSessions)
      .completedSessions(completedSessions)
      .totalSets(totalSets)
      .completedSets(completedSets)
      .avgCompletionPercentage(avgCompletion)
      .totalVolume(totalVolume)
      .totalDurationMinutes(totalDuration)
      .mostTrainedDayName(mostTrained.map(SessionResponse::getPlanDayName).orElse(null))
      .dailySummaries(
        sessionResponses.stream()
          .map(this::mapToDailySummary)
          .collect(Collectors.toList()))
      .build();
  }

  private SessionResponse mapToDailySummary(SessionResponse sessionResponse) {
    SessionResponse summary = new SessionResponse();
    summary.setId(sessionResponse.getId());
    summary.setPlanDayName(sessionResponse.getPlanDayName());
    summary.setStartedAt(sessionResponse.getStartedAt());
    summary.setEndedAt(sessionResponse.getEndedAt());
    summary.setStatus(sessionResponse.getStatus());
    summary.setTotalSets(sessionResponse.getTotalSets());
    summary.setCompletedSets(sessionResponse.getCompletedSets());
    summary.setCompletionPercentage(sessionResponse.getCompletionPercentage());
    summary.setTotalVolume(sessionResponse.getTotalVolume());
    summary.setDurationMinutes(sessionResponse.getDurationMinutes());
    summary.setPlanDayId(sessionResponse.getPlanDayId());
    summary.setCreatedAt(sessionResponse.getCreatedAt());
    summary.setSessionRpe(summary.getSessionRpe());
    summary.setNotes(sessionResponse.getNotes());
    summary.setSessionSets(sessionResponse.getSessionSets());
    summary.setPlannedItems(sessionResponse.getPlannedItems());
    return summary;
  }

  private Specification<Session> buildSessionSpecification(
      UUID userId, SessionSearchRequest sessionSearchRequest) {
    Specification<Session> spec =
        ((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId));

    if (sessionSearchRequest.getStatus() != null) {
      spec = spec.and(SessionSpecification.hasStatus(sessionSearchRequest.getStatus()));
    }

    if (sessionSearchRequest.getKeyword() != null
        && !sessionSearchRequest.getKeyword().trim().isEmpty()) {
      String likeKeyword = "%" + sessionSearchRequest.getKeyword().toLowerCase() + "%";
      spec = spec.and(SessionSpecification.hasKeyword(likeKeyword));
    }

    return spec;
  }

  private Session createSessionEntity(
      User user, PlanDay planDay, CreateStartSessionRequest request) {
    Session session = new Session();
    session.setUser(user);
    session.setPlanDay(planDay);
    session.setStatus(SessionStatus.IN_PROGRESS);
    session.setStartedAt(
        request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
    session.setNotes(request.getNotes());
    session.setCreatedAt(LocalDateTime.now());

    return session;
  }

  // Helper methods
  private SessionResponse convertSessionToResponse(Session session, boolean includeSessionSets) {
    SessionResponse dto = new SessionResponse();
    dto.setId(session.getId());
    dto.setPlanDayId(session.getPlanDay().getId());
    dto.setPlanDayName(session.getPlanDay().getSplitName());
    dto.setStartedAt(session.getStartedAt());
    dto.setEndedAt(session.getEndedAt());
    dto.setStatus(session.getStatus());
    dto.setNotes(session.getNotes());
    dto.setCreatedAt(session.getCreatedAt());

    // Calculate duration
    if (session.getEndedAt() != null) {
      long minutes =
          java.time.Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes();
      dto.setDurationMinutes((int) minutes);
    }

    if (includeSessionSets && session.getSessionSets() != null) {
      List<SessionSetResponse> sessionSetDtos =
          session.getSessionSets().stream().map(this::convertSessionSetToResponse).toList();
      dto.setSessionSets(sessionSetDtos);

      // Calculate summary stats
      dto.setTotalSets(sessionSetDtos.size());
      dto.setCompletedSets(
          (int) sessionSetDtos.stream().filter(SessionSetResponse::getIsCompleted).count());
      dto.setCompletionPercentage(
          dto.getTotalSets() > 0
              ? (double) dto.getCompletedSets() / dto.getTotalSets() * 100
              : 0.0);
      dto.setTotalVolume(
          sessionSetDtos.stream().mapToInt(s -> s.getVolume() != null ? s.getVolume() : 0).sum());
    }

    return dto;
  }

  private SessionSetResponse convertSessionSetToResponse(SessionSet sessionSet) {
    SessionSetResponse dto = new SessionSetResponse();
    dto.setId(sessionSet.getId());
    dto.setSessionId(sessionSet.getSession().getId());
    dto.setExerciseId(sessionSet.getExercise().getId());
    dto.setExerciseName(sessionSet.getExercise().getName());
    dto.setSetIndex(sessionSet.getSetIndex());
    dto.setPlanned(sessionSet.getPlanned());
    dto.setActual(sessionSet.getActual());
    dto.setCreatedAt(sessionSet.getCreatedAt());

    // Determine completion status
    JsonNode actual = sessionSet.getActual();
    dto.setIsSkipped(actual.has("isSkipped") && actual.get("isSkipped").asBoolean());
    dto.setIsCompleted(actual.has("completedAt") && !dto.getIsSkipped());

    if (dto.getIsCompleted() && actual.has("completedAt")) {
      dto.setCompletedAt(LocalDateTime.parse(actual.get("completedAt").asText()));
    }

    // Calculate volume
    if (actual.has("reps") && actual.has("weight")) {
      int reps = actual.get("reps").asInt();
      double weight = actual.get("weight").asDouble();
      dto.setVolume((int) (reps * weight));
    }

    // Performance comparison
    dto.setPerformanceComparison(calculatePerformanceComparison(sessionSet.getPlanned(), actual));

    return dto;
  }

  private String calculatePerformanceComparison(JsonNode planned, JsonNode actual) {
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

  private void updatePlanProgression(Session completedSession) {
    log.info("Updating plan progression for completed session: {}", completedSession.getId());

    // This could include:
    // 1. Check if all exercises showed improvement -> suggest weight increase
    // 2. Check if user consistently hits upper rep range -> progression
    // 3. Update plan based on performance trends
    // 4. Generate notifications/recommendations
    // 5. Update user statistics

    // For now, just log the completion
    log.info(
        "Session completed - plan progression check completed for session: {}",
        completedSession.getId());
  }
}
