package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.SessionSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.*;

import java.util.UUID;

public interface SessionManagementService {
  SessionResponse startSession(CreateStartSessionRequest request);

  SessionResponse getActiveSession();

  SessionResponse completeSession(UUID sessionId, CompleteSessionRequest request);

  SessionSetResponse updateSessionSet(
    UUID sessionSetId, UpdateSessionSetRequest request);

  void cancelSession( UUID sessionId, String reason);

  void pauseSession(UUID sessionId, String reason);

  SessionResponse getSessionDetails(UUID sessionId);
  SessionResponse getSummaryDay(UUID planDayId);

  SessionResponse resumeSession(UUID sessionId);

  PagedResponse<SessionResponse> getAllSessions(SessionSearchRequest request);

  WeeklySummaryResponse getSummaryWeekSessions();

  MonthlySummaryResponse getSummaryMonthSessions();

  PlanSummaryResponse getPlanSummary(UUID planId);
}
