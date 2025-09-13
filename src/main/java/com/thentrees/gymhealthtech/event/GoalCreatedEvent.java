package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.model.Goal;
import com.thentrees.gymhealthtech.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalCreatedEvent {
  private final Goal goal;
  private final UserProfile userProfile;
}
