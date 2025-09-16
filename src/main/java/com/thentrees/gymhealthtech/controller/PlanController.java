package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
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
@RequestMapping("/api/v1/users/plans")
@Slf4j
@RequiredArgsConstructor
public class PlanController {

  private final CustomPlanService customPlanService;
  private final UserService userService;

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
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PagedResponse<PlanSummaryResponse>>> getAllPlans(
      @RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false) List<PlanSourceType> planSourceTypes,
      @RequestParam(required = false) List<PlanStatusType> statusTypes,
      @RequestParam(required = false) List<UUID> goalIds,
      @RequestParam(required = false, defaultValue = "false") String hasActiveGoal,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection,
      @AuthenticationPrincipal UserDetails userDetails) {

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

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("GET /users/plans - User {} fetching all plans", userId);
    PagedResponse<PlanSummaryResponse> plans =
        customPlanService.getAllPlansForUser(userId, searchRequest, pageable);
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
    String email = authentication.getName();
    log.info("POST /users/plans - User {} creating custom plan: {}", email, request.getTitle());

    PlanResponse plan = customPlanService.createCustomPlan(email, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(plan));
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
      @PathVariable UUID planId, @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("GET /users/plans/{} - User {} fetching plan details", planId, userId);

    PlanResponse plan = customPlanService.getPlanDetails(userId, planId);

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
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("PUT /users/plans/{} - User {} updating plan", planId, userId);

    PlanResponse plan = customPlanService.updatePlan(userId, planId, request);

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
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<Void>> deletePlan(
      @PathVariable UUID planId, @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("DELETE /users/plans/{} - User {} deleting plan", planId, userId);

    customPlanService.deletePlan(userId, planId);

    return ResponseEntity.ok(APIResponse.success(null, "Plan deleted successfully"));
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
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("POST /users/plans/{}/days - User {} adding day to plan", planId, userId);

    PlanDayResponse planDay = customPlanService.addDayToPlan(userId, planId, request);

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
  public ResponseEntity<APIResponse<Void>> removeDayFromPlan(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "DELETE /users/plans/{}/days/{} - User {} removing day from plan",
        planId,
        planDayId,
        userId);

    customPlanService.removeDayFromPlan(userId, planId, planDayId);

    return ResponseEntity.ok(APIResponse.success(null, "Day removed from plan successfully"));
  }

  // Plan Day Management APIs
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
      @PathVariable String planId,
      @PathVariable String planDayId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "GET /users/plans/{}/days/{} - User {} fetching day details", planId, planDayId, userId);

    // This would need to be implemented in the service
    PlanDayResponse planDay =
        customPlanService.getPlanDayDetails(
            userId, UUID.fromString(planId), UUID.fromString(planDayId));

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
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PlanDayResponse>> updatePlanDay(
      @PathVariable String planId,
      @PathVariable String planDayId,
      @Valid @RequestBody UpdatePlanDayRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("PUT /users/plans/{}/days/{} - User {} updating day", planId, planDayId, userId);

    PlanDayResponse planDay =
        customPlanService.updatePlanDay(
            userId, UUID.fromString(planId), UUID.fromString(planDayId), request);

    return ResponseEntity.ok(APIResponse.success(planDay, "Day updated successfully"));
  }

  //  // Plan Item Management APIs
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
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "POST /users/plans/{}/days/{}/items - User {} adding item to day",
        planId,
        planDayId,
        userId);

    PlanItemResponse planItem =
        customPlanService.addItemToPlanDay(userId, planId, planDayId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(APIResponse.success(planItem, "Item added to day successfully"));
  }

  @Operation(
      method = "PUT",
      summary = "Update Plan Item",
      description =
          "Update details of a specific item within a plan day for the authenticated user.")
  @ApiResponses({
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
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "PUT /users/plans/{}/days/{}/items/{} - User {} updating item",
        planId,
        planDayId,
        planItemId,
        userId);

    PlanItemResponse planItem =
        customPlanService.updatePlanItem(userId, planId, planDayId, planItemId, request);

    return ResponseEntity.ok(APIResponse.success(planItem, "Item updated successfully"));
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
  public ResponseEntity<APIResponse<Void>> removePlanItem(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @PathVariable UUID planItemId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "DELETE /users/plans/{}/days/{}/items/{} - User {} removing item",
        planId,
        planDayId,
        planItemId,
        userId);

    customPlanService.removePlanItem(userId, planId, planDayId, planItemId);

    return ResponseEntity.ok(APIResponse.success(null, "Item removed successfully"));
  }

  @PostMapping("/{planId}/days/{planDayId}/items/bulk")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<Void>> addMultipleItemsToPlanDay(
      @PathVariable UUID planId,
      @PathVariable UUID planDayId,
      @Valid @RequestBody AddMultipleItemsRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "POST /users/plans/{}/days/{}/items/bulk - User {} adding multiple items",
        planId,
        planDayId,
        userId);

    customPlanService.addMultipleItemsToPlanDay(userId, planId, planDayId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(APIResponse.success(null, "Items added successfully"));
  }

  //  // Statistics and Analytics
  //    @GetMapping("/{planId}/stats")
  //    @PreAuthorize("hasRole('USER')")
  //    public ResponseEntity<PlanStatsDto> getPlanStatistics(
  //      @PathVariable UUID planId,
  //      @AuthenticationPrincipal UserDetails userDetails) {
  //
  //      UUID userId = getCurrentUserId(userDetails);
  //      log.info("GET /users/plans/{}/stats - User {} fetching plan statistics", planId, userId);
  //
  //      PlanStatsDto stats = customPlanService.getPlanStatistics(userId, planId);
  //
  //      return ResponseEntity.ok(stats);
  //    }

}
