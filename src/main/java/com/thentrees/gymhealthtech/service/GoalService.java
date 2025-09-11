package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateGoalRequest;
import com.thentrees.gymhealthtech.dto.response.GoalResponse;
import com.thentrees.gymhealthtech.model.User;
import java.util.List;

public interface GoalService {
  GoalResponse createGoal(User user, CreateGoalRequest request);

  List<GoalResponse> getUserGoals(String userId, boolean includeCompleted);

  GoalResponse getActiveGoal(String userId);
}
