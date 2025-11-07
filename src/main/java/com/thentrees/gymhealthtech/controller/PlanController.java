package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import com.thentrees.gymhealthtech.service.CustomPlanService;
import com.thentrees.gymhealthtech.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_V1 + "/users/plans")
@Slf4j
@RequiredArgsConstructor
public class PlanController {
  private final CustomPlanService customPlanService;
  @Operation(
      method = "GET",
      summary = "Get All Plans",
      description =
          "Retrieve a paginated list of all plans for the authenticated user, with optional filtering.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plans retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PagedResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping
  public ResponseEntity<APIResponse<PagedResponse<PlanSummaryResponse>>> getAllPlans(
      @RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false) List<PlanSourceType> planSourceTypes,
      @RequestParam(required = false) List<PlanStatusType> statusTypes,
      @RequestParam(required = false) List<UUID> goalIds,
      @RequestParam(required = false, defaultValue = "false") String hasActiveGoal,
      @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
      @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDirection,
      Authentication authentication) {

    PlanSearchRequest searchRequest =
        PlanSearchRequest.builder()
            .statusTypes(statusTypes)
            .sourceTypes(planSourceTypes)
            .goalIds(goalIds)
            .query(query)
            .hasActiveGoal(Boolean.parseBoolean(hasActiveGoal))
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

    PagedResponse<PlanSummaryResponse> plans =
        customPlanService.getAllPlansForUser(authentication, searchRequest, pageable);
    return ResponseEntity.ok(APIResponse.success(plans));
  }

  @Operation(
      method = "POST",
      summary = "Create Custom Plan",
      description = "Create a new custom plan for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Custom plan created successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PlanResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanResponse>> createCustomPlan(
      @Valid @RequestBody CreateCustomPlanRequest request, Authentication authentication) {
    PlanResponse plan = customPlanService.createCustomPlan(authentication, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(plan));
  }

  @Operation(
      method = "GET",
      summary = "Get User Plans, the endpoint is close",
      description = "Get all plans for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User plans retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = APIResponse.class))
            }),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping("/sample")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<List<PlanResponse>>> getUserPlans(
      Authentication authentication) {
    List<PlanResponse> plans = customPlanService.getUserPlans(authentication);
    return ResponseEntity.ok(APIResponse.success(plans, SuccessMessages.GET_PLAN_SUCCESS));
  }

  @Operation(
      method = "GET",
      summary = "Get Plan Details",
      description = "Retrieve details of a specific plan by its ID for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plan details retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PlanResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid plan ID format",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission to access this plan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @GetMapping("/{planId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanResponse>> getPlanDetails(
      @PathVariable UUID planId) {
    PlanResponse plan = customPlanService.getPlanDetails(planId);
    return ResponseEntity.ok(APIResponse.success(plan));
  }

  @Operation(
      method = "POST",
      summary = "Update Plan",
      description = "Update an existing plan for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plan updated successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PlanResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission to update this plan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @PostMapping("/{planId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanResponse>> updatePlan(
      @PathVariable UUID planId,
      @Valid @RequestBody UpdateCustomPlanRequest request,
      Authentication authentication) {
    PlanResponse plan = customPlanService.updatePlan(authentication, planId, request);
    return ResponseEntity.ok(APIResponse.success(plan));
  }

  @Operation(
      method = "DELETE",
      summary = "Delete Plan",
      description = "Delete an existing plan for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plan deleted successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Void.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid plan ID format",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User not authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have permission to delete this plan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Plan not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class)))
      })
  @DeleteMapping("/{planId}")
  public ResponseEntity<APIResponse<String>> deletePlan(
      @PathVariable UUID planId, Authentication authentication) {
    customPlanService.deletePlan(authentication, planId);
    return ResponseEntity.ok(APIResponse.success(SuccessMessages.DELETE_PLAN_SUCCESS));
  }

  @Operation(
      method = "POST",
      summary = "Add Day to Plan",
      description = "Add a new day to an existing plan for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Day added to plan successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PlanDayResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @PostMapping("/{planId}/days")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanDayResponse>> addDayToPlan(
      @PathVariable UUID planId,
      @Valid @RequestBody CreateCustomPlanDayRequest request,
      Authentication authentication) {
    PlanDayResponse planDay = customPlanService.addDayToPlan(authentication, planId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(planDay));
  }

  @Operation(
      method = "DELETE",
      summary = "Remove Day from Plan",
      description = "Remove a day from an existing plan for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Day removed from plan successfully",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid plan or day ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan or Day not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @DeleteMapping("/{planId}/days/{planDayId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<String>> removeDayFromPlan(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      Authentication authentication) {
    customPlanService.removeDayFromPlan(authentication, planId, planDayId);
    return ResponseEntity.ok(APIResponse.success(SuccessMessages.DELETE_PLAN_DAY_SUCCESS));
  }

  @Operation(
      method = "GET",
      summary = "Get Plan Day Details",
      description = "Retrieve details of a specific day within a plan for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Plan day details retrieved successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PlanDayResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid plan or day ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to access this plan day",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan or Day not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @GetMapping("/{planId}/days/{planDayId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanDayResponse>> getPlanDayDetails(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      Authentication authentication) {
    PlanDayResponse planDay =
        customPlanService.getPlanDayDetails(
            authentication,planId, planDayId);
    return ResponseEntity.ok(APIResponse.success(planDay));
  }

  @Operation(
      method = "PUT",
      summary = "Update Plan Day",
      description = "Update details of a specific day within a plan for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Plan day updated successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PlanDayResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data or ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan day",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan or Day not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @PutMapping("/{planId}/days/{planDayId}")
  public ResponseEntity<APIResponse<PlanDayResponse>> updatePlanDay(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @Valid @RequestBody UpdatePlanDayRequest request,
      Authentication authentication) {
    PlanDayResponse planDay =
      customPlanService.updatePlanDay(authentication,planId,planDayId, request);
    return ResponseEntity.ok(APIResponse.success(planDay, SuccessMessages.UPDATE_PLAN_DAY_SUCCESS));
  }

  @Operation(
      method = "POST",
      summary = "Add Item to Plan Day",
      description = "Add a new item to a specific day within a plan for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Item added to day successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PlanItemResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data or ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan day",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan or Day not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @PostMapping("/{planId}/days/{planDayId}/items")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanItemResponse>> addItemToPlanDay(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @Valid @RequestBody CreateCustomPlanItemRequest request,
      Authentication authentication
  ) {
    PlanItemResponse planItem =
        customPlanService.addItemToPlanDay(authentication, planId, planDayId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(APIResponse.success(planItem));
  }

  @Operation(
      method = "PUT",
      summary = "Update Plan Item",
      description =
          "Update details of a specific item within a plan day for the authenticated user.")
  @ApiResponses( value = {
    @ApiResponse(
        responseCode = "200",
        description = "Plan item updated successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PlanItemResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data or ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan item",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan, Day, or Item not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @PutMapping("/{planId}/days/{planDayId}/items/{planItemId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanItemResponse>> updatePlanItem(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @PathVariable UUID planItemId,
      @Valid @RequestBody UpdatePlanItemRequest request,
      Authentication authentication) {
    PlanItemResponse planItem =
        customPlanService.updatePlanItem(authentication, planId, planDayId, planItemId, request);
    return ResponseEntity.ok(APIResponse.success(planItem, SuccessMessages.UPDATE_PLAN_ITEM_SUCCESS));
  }

  @Operation(
      method = "DELETE",
      summary = "Remove Plan Item",
      description = "Remove a specific item from a plan day for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Plan item removed successfully",
        content = {
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = APIResponse.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid ID format",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - User not authenticated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - User does not have permission to modify this plan item",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Plan, Day, or Item not found",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = APIResponse.class)))
  })
  @DeleteMapping("/{planId}/days/{planDayId}/items/{planItemId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<String>> removePlanItem(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @PathVariable UUID planItemId,
      Authentication authentication) {
    customPlanService.removePlanItem(authentication, planId, planDayId, planItemId);
    return ResponseEntity.ok(APIResponse.success(SuccessMessages.DELETE_PLAN_ITEM_SUCCESS));
  }

  @PostMapping("/{planId}/days/{planDayId}/items/bulk")
  public ResponseEntity<APIResponse<String>> addMultipleItemsToPlanDay(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @Valid @RequestBody AddMultipleItemsRequest request,
      Authentication authentication) {
    customPlanService.addMultipleItemsToPlanDay(authentication, planId, planDayId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(APIResponse.success(SuccessMessages.CREATE_PLAN_ITEM_SUCCESS));
  }
}
