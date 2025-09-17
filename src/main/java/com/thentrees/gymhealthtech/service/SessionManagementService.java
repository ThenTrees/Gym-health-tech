package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.SessionResponse;
import com.thentrees.gymhealthtech.dto.response.SessionSetResponse;
import java.util.UUID;

public interface SessionManagementService {
  SessionResponse startSession(UUID userId, CreateStartSessionRequest request);

  SessionResponse getActiveSession(UUID userId);

  SessionResponse completeSession(UUID userId, UUID sessionId, CompleteSessionRequest request);

  SessionSetResponse updateSessionSet(
      UUID userId, UUID sessionSetId, UpdateSessionSetRequest request);
}
