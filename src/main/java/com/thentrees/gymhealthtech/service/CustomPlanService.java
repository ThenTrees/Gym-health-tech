package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.PlanDayResponse;
import com.thentrees.gymhealthtech.dto.response.PlanItemResponse;
import com.thentrees.gymhealthtech.dto.response.PlanResponse;
import java.util.List;
import java.util.UUID;

public interface CustomPlanService {
  PlanResponse createCustomPlan(String email, CreateCustomPlanRequest request);

  List<PlanResponse> getUserPlans(UUID userId);

  PlanResponse getPlanDetails(UUID userId, UUID planId);

  PlanResponse updatePlan(UUID userId, UUID planId, UpdateCustomPlanRequest request);

  void deletePlan(UUID userId, UUID planId);

  PlanDayResponse addDayToPlan(UUID userId, UUID planId, CreateCustomPlanDayRequest request);

  void removeDayFromPlan(UUID userId, UUID planId, UUID planDayId);

  PlanDayResponse getPlanDayDetails(UUID userId, UUID planId, UUID planDayId);

  PlanDayResponse updatePlanDay(
      UUID userId, UUID planId, UUID planDayId, UpdatePlanDayRequest request);

  PlanItemResponse addItemToPlanDay(
      UUID userId, UUID planId, UUID planDayId, CreateCustomPlanItemRequest request);

  PlanItemResponse updatePlanItem(
      UUID userId, UUID planId, UUID planDayId, UUID planItemId, UpdatePlanItemRequest request);

  void removePlanItem(UUID userId, UUID planId, UUID planDayId, UUID planItemId);

  void addMultipleItemsToPlanDay(
      UUID userId, UUID planId, UUID planDayId, AddMultipleItemsRequest request);
}
