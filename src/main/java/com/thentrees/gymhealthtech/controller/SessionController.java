package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.SessionSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.enums.SessionStatus;
import com.thentrees.gymhealthtech.service.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(AppConstants.API_V1 + "/users/sessions")
@RequiredArgsConstructor
public class SessionController {
  private final SessionManagementService sessionService;

  @Operation(
      method = "POST",
      summary = "Start a new workout session",
      description =
          "Starts a new workout session for the authenticated user based on a specified plan day")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Session started successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='createStartSessionRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/start")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> startSession(
      @Valid @RequestBody CreateStartSessionRequest request) {
    SessionResponse session = sessionService.startSession(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(session));
  }

  @Operation(
      method = "GET",
      summary = "Get active workout session",
      description = "Retrieves the currently active workout session for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active session retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "404",
            description = "No active session found for the user",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "No active session found for user",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/active")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> getActiveSession() {
    SessionResponse session = sessionService.getActiveSession();
    return ResponseEntity.ok(APIResponse.success(session));
  }

  @Operation(
      method = "POST",
      summary = "Complete a workout session",
      description =
          "Completes an active workout session for the authenticated user with optional notes")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session completed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='completeSessionRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
      })
  @PostMapping("/{sessionId}/complete")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> completeSession(
      @PathVariable UUID sessionId,
      @Valid @RequestBody CompleteSessionRequest request
      ) {
    SessionResponse session = sessionService.completeSession(sessionId, request);
    return ResponseEntity.ok(APIResponse.success(session));
  }


  @Operation(
      method = "PUT",
      summary = "Update a session set",
      description =
          "Updates details of a specific session set for the authenticated user, such as reps or weight")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session set updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionSetResponse.class),
                    examples = @ExampleObject())),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='updateSessionSetRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/sets/{sessionSetId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionSetResponse>> updateSessionSet(
      @PathVariable UUID sessionSetId,
      @Valid @RequestBody UpdateSessionSetRequest request) {
    SessionSetResponse sessionSet = sessionService.updateSessionSet(sessionSetId, request);
    return ResponseEntity.ok(APIResponse.success(sessionSet));
  }

  @Operation(
      method = "POST",
      summary = "Cancel a workout session",
      description =
          "Cancels an active workout session for the authenticated user with an optional reason")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cancel session successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "success",
              "message": "Session cancelled successfully",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='cancelSessionRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{sessionId}/cancel")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<Void>> cancelSession(
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String reason
    ) {
    sessionService.cancelSession(sessionId, reason);
    return ResponseEntity.ok(APIResponse.success(null, "Session cancelled successfully"));
  }

  @Operation(
      method = "POST",
      summary = "Pause a workout session",
      description =
          "Pauses an active workout session for the authenticated user with an optional reason")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session paused successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "success",
              "message": "Session paused successfully",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='pauseSessionRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{sessionId}/pause")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<String>> pauseSession(
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String reason) {
    sessionService.pauseSession(sessionId, reason);
    return ResponseEntity.ok(APIResponse.success("Session paused successfully"));
  }

  @Operation(
      method = "GET",
      summary = "Get session details",
      description =
          "Retrieves detailed information about a specific workout session for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session details retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "404",
            description = "Session not found",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Session not found",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{sessionId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> getSessionDetails(
      @PathVariable UUID sessionId) {
    SessionResponse session = sessionService.getSessionDetails(sessionId);
    return ResponseEntity.ok(APIResponse.success(session));
  }

  @Operation(
      method = "POST",
      summary = "Resume a paused workout session",
      description =
          "Resumes a previously paused workout session for the authenticated user with an optional reason")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session resumed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Validation failed for object='resumeSessionRequest'. Error count: 1",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{sessionId}/resume")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> resumeSession(
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String reason) {
    SessionResponse sessionResponse = sessionService.resumeSession(sessionId);
    return ResponseEntity.ok(APIResponse.success(sessionResponse, "Session resumed successfully"));
  }

  @Operation(
      method = "GET",
      summary = "Get all sessions",
      description = "Retrieves a list of all workout sessions for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sessions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SessionResponse.class),
                    examples = @ExampleObject("""
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Full authentication is required to access this resource",
              "data": null
            }
            """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            """
            {
              "status": "error",
              "message": "Access is denied",
              "data": null
            }
            """))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<PagedResponse<SessionResponse>>> getAllSessions(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
      @RequestParam(value = "direction", defaultValue = "desc") String direction,
      @RequestParam(value = "status", required = false) SessionStatus status,
      @RequestParam(value = "keyword", required = false) String keyword) {

    SessionSearchRequest sessionSearchRequest =
        SessionSearchRequest.builder()
            .keyword(keyword)
            .status(status)
            .page(page)
            .size(size)
            .sortBy(sort)
            .sortOrder(direction)
            .build();

    PagedResponse<SessionResponse> sessions =
        sessionService.getAllSessions(sessionSearchRequest);
    return ResponseEntity.ok(APIResponse.success(sessions));
  }


  // summary info workout sessions
  @GetMapping("/summary/week/current")
  public ResponseEntity<APIResponse<WeeklySummaryResponse>> getSummarySessions() {
    return ResponseEntity.ok(APIResponse.success(
      sessionService.getSummaryWeekSessions()
    ));
  }

  @GetMapping("/summary/day/current")
  public ResponseEntity<APIResponse<SessionResponse>> getSummaryDaySessions(
    @RequestParam("planDayId") UUID planDayId) {

    SessionResponse session = sessionService.getSummaryDay(planDayId);

    return ResponseEntity.ok(APIResponse.success(session));
  }
}
