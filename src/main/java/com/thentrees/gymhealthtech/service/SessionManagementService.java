package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.SessionSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.*;

import java.util.UUID;

public interface SessionManagementService {
  SessionResponse startSession(UUID userId, CreateStartSessionRequest request);

  SessionResponse getActiveSession(UUID userId);

  SessionResponse completeSession(UUID userId, UUID sessionId, CompleteSessionRequest request);

  SessionSetResponse updateSessionSet(
      UUID userId, UUID sessionSetId, UpdateSessionSetRequest request);

  void cancelSession(UUID userId, UUID sessionId, String reason);

  void pauseSession(UUID userId, UUID sessionId, String reason);

  SessionResponse getSessionDetails(UUID userId, UUID sessionId);
  SessionResponse getSummaryDay(UUID userId, UUID planDayId);

  SessionResponse resumeSession(UUID userId, UUID sessionId);

  PagedResponse<SessionResponse> getAllSessions(UUID userId, SessionSearchRequest request);

  WeeklySummaryResponse getSummaryWeekSessions(UUID userId);
}
