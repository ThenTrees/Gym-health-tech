package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.model.PlanDay;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface CustomPlanService {
  PlanResponse createCustomPlan(Authentication authentication, CreateCustomPlanRequest request);

  List<PlanResponse> getUserPlans(Authentication authentication);

  PlanResponse getPlanDetails(UUID planId);

  PlanResponse updatePlan(Authentication authentication, UUID planId, UpdateCustomPlanRequest request);

  void deletePlan(Authentication authentication, UUID planId);

  PlanDayResponse addDayToPlan(Authentication authentication, UUID planId, CreateCustomPlanDayRequest request);

  void removeDayFromPlan(Authentication authentication, UUID planId, UUID planDayId);

  PlanDayResponse getPlanDayDetails(Authentication authentication, UUID planId, UUID planDayId);

  PlanDayResponse updatePlanDay(
    Authentication authentication, UUID planId, UUID planDayId, UpdatePlanDayRequest request);

  PlanItemResponse addItemToPlanDay(
      Authentication authentication, UUID planId, UUID planDayId, CreateCustomPlanItemRequest request);

  PlanItemResponse updatePlanItem(
      Authentication authentication, UUID planId, UUID planDayId, UUID planItemId, UpdatePlanItemRequest request);

  void removePlanItem(Authentication authentication, UUID planId, UUID planDayId, UUID planItemId);

  void addMultipleItemsToPlanDay(
      Authentication authentication, UUID planId, UUID planDayId, AddMultipleItemsRequest request);

  PagedResponse<PlanSummaryResponse> getAllPlansForUser(
    Authentication authentication, PlanSearchRequest searchCriteria, Pageable pageable);

  PlanDay duplicatePlanDayForNextWeek(PlanDay planDay);

  void usePlan(UUID planId);
}
