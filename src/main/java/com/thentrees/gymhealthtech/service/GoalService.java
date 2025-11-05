package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateGoalRequest;
import com.thentrees.gymhealthtech.dto.response.GoalResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface GoalService {
  GoalResponse createGoal(Authentication authentication, CreateGoalRequest request);

  List<GoalResponse> getUserGoals(Authentication authentication, boolean includeCompleted);

  GoalResponse getActiveGoal(Authentication authentication);

  GoalResponse updateGoal(Authentication authentication, UUID goalId, CreateGoalRequest request);
}
