package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.SessionSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.DifficultyLevel;
import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.exception.ValidationException;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.*;
import com.thentrees.gymhealthtech.repository.spec.SessionSpecification;
import com.thentrees.gymhealthtech.service.SessionManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j(topic = "SESSION-SERVICE")
@RequiredArgsConstructor
public class SessionManagementServiceImpl implements SessionManagementService {

  private final PlanDayRepository planDayRepository;
  private final SessionRepository sessionRepository;
  private final PlanItemRepository planItemRepository;
  private final SessionSetRepository sessionSetRepository;
  private final ObjectMapper objectMapper;
  private final PlanRepository planRepository;

  public SessionResponse getSessionDetails(UUID sessionId) {
    User user = getCurrentUser();
    Session session =
        sessionRepository
            .findByIdAndUserIdWithSets(sessionId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId.toString()));
    return convertSessionToResponse(session, true);
  }

  @Transactional(readOnly = true)
  @Override
  public SessionResponse getSummaryDay(UUID planDayId) {
    User user = getCurrentUser();
    Session session =
      sessionRepository
        .findByPlanDayIdAndUserIdWithSets(planDayId, user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Session", planDayId.toString()));
    return convertSessionToResponse(session, true);
  }

  @Override
  @Transactional
  public SessionResponse startSession(CreateStartSessionRequest request) {

    User user = getCurrentUser();
    PlanDay planDay =
        planDayRepository
            .findByIdAndPlanUserId(request.getPlanDayId(), user.getId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Plan day not found or not associated with user"));

    // Check if there is already an active session for the user
    Optional<Session> activeSession =
        sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.IN_PROGRESS);
    if (activeSession.isPresent()) {
      log.error("Session already active for user {} with plan day {}", user.getId(), planDay.getId());
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

    return convertSessionToResponse(session, true);
  }

  @Override
  public SessionResponse getActiveSession() {

    User user = getCurrentUser();

    Session session =
        sessionRepository
            .findByUserIdAndStatus(user.getId(), SessionStatus.IN_PROGRESS)
            .orElseThrow(() -> new ResourceNotFoundException("No active workout session found"));

    return convertSessionToResponse(session, true);
  }

  @Override
  @Transactional
  public SessionResponse completeSession(
    UUID sessionId, CompleteSessionRequest request) {

    User user = getCurrentUser();

    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
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
      UUID sessionSetId, UpdateSessionSetRequest request) {
    User user = getCurrentUser();
    SessionSet sessionSet =
        sessionSetRepository
            .findByIdAndSessionUserId(sessionSetId, user.getId())
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
  public void cancelSession(UUID sessionId, String reason) {
    log.info("Cancelling session {} for user {} - reason: {}", sessionId, reason);
    User user = getCurrentUser();
    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
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
  public void pauseSession(UUID sessionId, String reason) {
    User user = getCurrentUser();
    Session session =
        sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
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
  public SessionResponse resumeSession(UUID sessionId) {
    log.info("Resuming session {}", sessionId);
    User user = getCurrentUser();
    Session session =
          sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
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
  public PagedResponse<SessionResponse> getAllSessions(SessionSearchRequest request) {

    User user = getCurrentUser();

    Specification<Session> spec = buildSessionSpecification(user.getId(), request);

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
  public WeeklySummaryResponse getSummaryWeekSessions() {
    User user = getCurrentUser();
    LocalDate today = LocalDate.now();
    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    List<Session> sessions =
        sessionRepository.findByUserAndStartedAtBetween(
            user,
            startOfWeek.atStartOfDay(),
            endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1));

    if (sessions.isEmpty()) {
      return buildEmptyWeeklySummary(startOfWeek, endOfWeek);
    }

    List<SessionResponse> sessionResponses = buildSessionResponses(sessions, true);
    SessionStatistics statistics = computeSessionStatistics(sessionResponses);

    String mostTrainedDayName =
        sessionResponses.stream()
            .max(
                Comparator.comparingInt(
                    summary -> Optional.ofNullable(summary.getTotalVolume()).orElse(0)))
            .map(SessionResponse::getPlanDayName)
            .orElse(null);

    return WeeklySummaryResponse.builder()
        .weekStart(startOfWeek)
        .weekEnd(endOfWeek)
        .totalSessions(statistics.totalSessions())
        .completedSessions(statistics.completedSessions())
        .totalSets(statistics.totalSets())
        .completedSets(statistics.completedSets())
        .avgCompletionPercentage(statistics.avgCompletionPercentage())
        .totalVolume(statistics.totalVolume())
        .totalDurationMinutes(statistics.totalDurationMinutes())
        .mostTrainedDayName(mostTrainedDayName)
        .dailySummaries(sessionResponses.stream().map(this::mapToDailySummary).toList())
        .build();
  }

  @Override
  public MonthlySummaryResponse getSummaryMonthSessions() {
    User user = getCurrentUser();
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
    List<Session> sessions = sessionRepository.findByUserAndAllSessionsInCurrentMonth(
      user.getId(), startOfMonth, endOfMonth
    );

    if (sessions.isEmpty()) {
      return buildEmptyMonthlySummary();
    }

    List<SessionResponse> sessionResponses = buildSessionResponses(sessions, true);
    SessionStatistics statistics = computeSessionStatistics(sessionResponses);

    double completionRate = statistics.completionRate();
    double avgCompletionPercentage = statistics.avgCompletionPercentage();
    double avgRpe = statistics.avgRpe();

    double avgVolumePerSession = statistics.avgVolumePerCompletedSession();
    double avgDurationPerSession = statistics.avgDurationPerCompletedSession();

    String feedback = generateFeedback(completionRate, avgRpe, avgCompletionPercentage);

    return MonthlySummaryResponse.builder()
        .month(startOfMonth.toLocalDate())
        .totalSessions(statistics.totalSessions())
        .completedSessions(statistics.completedSessions())
        .completionRate(completionRate)
        .totalSets(statistics.totalSets())
        .completedSets(statistics.completedSets())
        .avgCompletionPercentage(avgCompletionPercentage)
        .totalVolume(statistics.totalVolume())
        .avgVolumePerSession(avgVolumePerSession)
        .totalDurationMinutes(statistics.totalDurationMinutes())
        .avgDurationPerSession(avgDurationPerSession)
        .avgRpe(avgRpe)
        .weeklySummaries(sessionResponses.stream().map(this::mapToDailySummary).toList())
        .feedback(feedback)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PlanSummaryResponse getPlanSummary(UUID planId) {

    User user = getCurrentUser();
    Plan plan =
        planRepository
            .findByIdAndUserId(planId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));

    List<PlanDay> planDays =
        Optional.ofNullable(plan.getPlanDays())
            .filter(days -> !days.isEmpty())
            .orElseGet(() -> planDayRepository.findAllByPlanId(planId));
    if (planDays == null) {
      planDays = Collections.emptyList();
    }

    List<Session> sessions = sessionRepository.findByPlanDayPlanIdAndUserId(planId, user.getId());
    Map<UUID, List<Session>> sessionsByDay =
        sessions.stream()
            .filter(session -> session.getPlanDay() != null)
            .collect(Collectors.groupingBy(session -> session.getPlanDay().getId()));

    int totalDays = planDays.size();
    int totalExercises =
        planDays.stream()
            .map(PlanDay::getPlanItems)
            .filter(Objects::nonNull)
            .mapToInt(List::size)
            .sum();

    int totalSessions = sessions.size();
    long completedSessions =
        sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
    long cancelledSessions =
        sessions.stream().filter(s -> s.getStatus() == SessionStatus.CANCELLED).count();

    long totalDurationMinutes =
        sessions.stream()
            .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
            .mapToLong(s -> Duration.between(s.getStartedAt(), s.getEndedAt()).toMinutes())
            .sum();

    double totalCalories = 0d;

    BigDecimal completionRate =
        totalSessions == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(completedSessions)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP);

    BigDecimal avgDurationPerSession =
        completedSessions == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(totalDurationMinutes)
                .divide(BigDecimal.valueOf(completedSessions), 2, RoundingMode.HALF_UP);

    BigDecimal avgCaloriesPerSession =
        totalSessions == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(totalCalories)
                .divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP);

    Double timelineProgressPercentage =
        totalDays == 0 ? 0.0 : Math.min(100.0, completedSessions * 100.0 / totalDays);

    LocalDateTime startedAt =
        sessions.stream()
            .map(Session::getStartedAt)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo)
            .orElse(null);

    LocalDateTime lastEndedAt =
        sessions.stream()
            .map(Session::getEndedAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

    LocalDate lastWorkoutDate =
        Optional.ofNullable(lastEndedAt)
            .map(LocalDateTime::toLocalDate)
            .orElseGet(
                () ->
                    sessions.stream()
                        .map(Session::getStartedAt)
                        .filter(Objects::nonNull)
                        .map(LocalDateTime::toLocalDate)
                        .max(LocalDate::compareTo)
                        .orElse(null));

    LocalDate today = LocalDate.now();

    int missedDays =
        (int)
            planDays.stream()
                .filter(day -> day.getScheduledDate() != null)
                .filter(day -> day.getScheduledDate().isBefore(today))
                .filter(
                    day ->
                        sessionsByDay
                            .getOrDefault(day.getId(), Collections.emptyList())
                            .stream()
                            .noneMatch(s -> s.getStatus() == SessionStatus.COMPLETED))
                .count();

    LocalDate nextScheduledDate =
        planDays.stream()
            .filter(day -> day.getScheduledDate() != null)
            .filter(day -> !day.getScheduledDate().isBefore(today))
            .filter(
                day ->
                    sessionsByDay
                        .getOrDefault(day.getId(), Collections.emptyList())
                        .stream()
                        .noneMatch(s -> s.getStatus() == SessionStatus.COMPLETED))
            .map(PlanDay::getScheduledDate)
            .min(LocalDate::compareTo)
            .orElse(null);

    List<String> mainMuscleGroups =
        planDays.stream()
            .map(PlanDay::getPlanItems)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(PlanItem::getExercise)
            .filter(Objects::nonNull)
            .map(
                exercise -> {
                  if (exercise.getPrimaryMuscle() != null) {
                    return exercise.getPrimaryMuscle().getName();
                  }
                  String bodyPart = exercise.getBodyPart();
                  return bodyPart != null && !bodyPart.isBlank() ? bodyPart : null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

    int estimatedPlanMinutes =
        planDays.stream()
            .map(PlanDay::getPlanItems)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .mapToInt(this::estimateExerciseDuration)
            .sum();

    Integer estimatedWeeklyHours = null;
    if (estimatedPlanMinutes > 0) {
      int weeks = plan.getCycleWeeks() != null && plan.getCycleWeeks() > 0 ? plan.getCycleWeeks() : 1;
      double weeklyHours = (double) estimatedPlanMinutes / weeks / 60.0;
      estimatedWeeklyHours = (int) Math.round(weeklyHours);
    }

    double avgExerciseLevel =
        planDays.stream()
            .map(PlanDay::getPlanItems)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(PlanItem::getExercise)
            .filter(Objects::nonNull)
            .map(Exercise::getLevel)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

    DifficultyLevel difficultyLevel =
        avgExerciseLevel > 0 ? resolveDifficultyLevel(avgExerciseLevel) : null;

    return PlanSummaryResponse.builder()
        .id(plan.getId())
        .title(plan.getTitle())
        .source(plan.getSource())
        .status(plan.getStatus())
        .totalWeeks(plan.getCycleWeeks())
        .createdAt(plan.getCreatedAt())
        .startedAt(startedAt != null ? startedAt : plan.getCreatedAt())
        .endedAt(
            plan.getEndDate() != null
                ? plan.getEndDate().atStartOfDay()
                : (lastEndedAt != null ? lastEndedAt : null))
        .totalCalories(totalCalories)
        .totalDurationMinutes(totalDurationMinutes)
        .totalSessions(totalSessions)
        .completionRate(completionRate)
        .avgCaloriesPerSession(avgCaloriesPerSession)
        .avgDurationPerSession(avgDurationPerSession)
        .totalExercises(totalExercises)
        .totalDays(totalDays)
        .completedSessions((int) completedSessions)
        .timelineProgressPercentage(timelineProgressPercentage)
        .lastWorkoutDate(lastWorkoutDate)
        .nextScheduledDate(nextScheduledDate)
        .skippedSessions((int) cancelledSessions)
        .missedDays(missedDays)
        .mainMuscleGroups(mainMuscleGroups)
        .estimatedWeeklyHours(estimatedWeeklyHours)
        .difficultyLevel(difficultyLevel)
        .build();
  }

  private List<SessionResponse> buildSessionResponses(List<Session> sessions, boolean includeSessionSets) {
    return sessions.stream()
        .map(session -> convertSessionToResponse(session, includeSessionSets))
        .toList();
  }

  private MonthlySummaryResponse buildEmptyMonthlySummary() {
    return MonthlySummaryResponse.builder()
      .totalSessions(0)
      .completedSessions(0)
      .completionRate(0.0)
      .totalSets(0)
      .completedSets(0)
      .avgCompletionPercentage(0.0)
      .totalVolume(0)
      .avgVolumePerSession(0.0)
      .totalDurationMinutes(0)
      .avgDurationPerSession(0.0)
      .avgRpe(0.0)
      .weeklySummaries(Collections.emptyList())
      .feedback("Chưa có dữ liệu tập luyện trong tháng này.")
      .build();
  }


  private WeeklySummaryResponse buildEmptyWeeklySummary(LocalDate startOfWeek, LocalDate endOfWeek) {
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

  private SessionResponse mapToDailySummary(SessionResponse sessionResponse) {
    SessionResponse summary = new SessionResponse();
    summary.setId(sessionResponse.getId());
    summary.setPlanDayId(sessionResponse.getPlanDayId());
    summary.setPlanDayName(sessionResponse.getPlanDayName());
    summary.setStartedAt(sessionResponse.getStartedAt());
    summary.setEndedAt(sessionResponse.getEndedAt());
    summary.setStatus(sessionResponse.getStatus());
    summary.setSessionRpe(sessionResponse.getSessionRpe());
    summary.setNotes(sessionResponse.getNotes());
    summary.setCreatedAt(sessionResponse.getCreatedAt());
    summary.setDurationMinutes(sessionResponse.getDurationMinutes());
    summary.setTotalSets(sessionResponse.getTotalSets());
    summary.setCompletedSets(sessionResponse.getCompletedSets());
    summary.setCompletionPercentage(sessionResponse.getCompletionPercentage());
    summary.setTotalVolume(sessionResponse.getTotalVolume());
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

  private SessionStatistics computeSessionStatistics(List<SessionResponse> sessionResponses) {
    int totalSessions = sessionResponses.size();
    int completedSessions =
        (int)
            sessionResponses.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .count();

    int totalSets =
        sessionResponses.stream()
            .map(SessionResponse::getTotalSets)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    int completedSets =
        sessionResponses.stream()
            .map(SessionResponse::getCompletedSets)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    int totalVolume =
        sessionResponses.stream()
            .map(SessionResponse::getTotalVolume)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    int totalDurationMinutes =
        sessionResponses.stream()
            .map(SessionResponse::getDurationMinutes)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    double avgCompletionPercentage =
        sessionResponses.stream()
            .map(SessionResponse::getCompletionPercentage)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    double avgRpe =
        sessionResponses.stream()
            .map(SessionResponse::getSessionRpe)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

    double avgVolumePerCompletedSession =
        completedSessions == 0 ? 0.0 : (double) totalVolume / completedSessions;

    double avgDurationPerCompletedSession =
        sessionResponses.stream()
                    .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                    .map(SessionResponse::getDurationMinutes)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

    return new SessionStatistics(
        totalSessions,
        completedSessions,
        totalSets,
        completedSets,
        totalVolume,
        totalDurationMinutes,
        avgCompletionPercentage,
        avgRpe,
        avgVolumePerCompletedSession,
        avgDurationPerCompletedSession);
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

  private int estimateExerciseDuration(PlanItem planItem) {
    try {
      JsonNode prescription = planItem.getPrescription();
      int sets = prescription.path("sets").asInt(1);
      int restSeconds = prescription.path("restSeconds").asInt(90);
      int tempoSeconds = prescription.path("tempoSeconds").asInt(0);

      int perSetSeconds = tempoSeconds > 0 ? tempoSeconds : 40;
      int totalRestSeconds = Math.max(0, sets - 1) * restSeconds;
      int totalSeconds = sets * perSetSeconds + totalRestSeconds;
      return Math.max(1, totalSeconds / 60);
    } catch (Exception e) {
      return 5;
    }
  }

  private DifficultyLevel resolveDifficultyLevel(double avgLevel) {
    if (avgLevel >= 2.5) {
      return DifficultyLevel.ADVANCED;
    }
    if (avgLevel >= 1.5) {
      return DifficultyLevel.INTERMEDIATE;
    }
    return DifficultyLevel.BEGINNER;
  }

  private User getCurrentUser(){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (User)authentication.getPrincipal();
  }

  private String generateFeedback(double completionRate, double avgRpe, double avgCompletion) {
    if (completionRate < 40) {
      return "Cố gắng duy trì thói quen tập luyện đều hơn nhé!";
    }
    if (completionRate < 70) {
      return "Bạn đang tiến bộ tốt! Hãy cố gắng hoàn thành các buổi còn lại.";
    }
    if (avgRpe > 8) {
      return "Cường độ cao! Hãy đảm bảo bạn nghỉ ngơi và hồi phục đủ.";
    }
    if (avgCompletion > 90) {
      return "Tuyệt vời! Bạn đã gần như hoàn thành toàn bộ mục tiêu trong tháng!";
    }
    return "Bạn đang duy trì phong độ tốt, tiếp tục nhé!";
  }

  private record SessionStatistics(
      int totalSessions,
      int completedSessions,
      int totalSets,
      int completedSets,
      int totalVolume,
      int totalDurationMinutes,
      double avgCompletionPercentage,
      double avgRpe,
      double avgVolumePerCompletedSession,
      double avgDurationPerCompletedSession) {
    double completionRate() {
      return totalSessions == 0 ? 0.0 : completedSessions * 100.0 / totalSessions;
    }
  }
}
