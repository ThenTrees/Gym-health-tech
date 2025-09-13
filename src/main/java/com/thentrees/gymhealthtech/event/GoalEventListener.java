package com.thentrees.gymhealthtech.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoalEventListener {
  //  private final AIFitnessPlanService aiPlanService;

  //  public GoalEventListener(AIFitnessPlanService aiPlanService) {
  //    this.aiPlanService = aiPlanService;
  //  }

  @Async
  @EventListener
  public void handleGoalCreated(GoalCreatedEvent event) {
    try {
      log.info(
          "Goal created event received, generating AI plan for goal: {}", event.getGoal().getId());
      //      aiPlanService.generatePlanForGoal(event.getGoal(), event.getUserProfile());
    } catch (Exception e) {
      log.error("Failed to generate AI plan for goal: {}", event.getGoal().getId(), e);
      // Could implement retry logic or notification here
    }
  }
}
