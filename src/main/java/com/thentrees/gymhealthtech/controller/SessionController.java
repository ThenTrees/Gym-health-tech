package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CompleteSessionRequest;
import com.thentrees.gymhealthtech.dto.request.CreateStartSessionRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateSessionSetRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.SessionResponse;
import com.thentrees.gymhealthtech.dto.response.SessionSetResponse;
import com.thentrees.gymhealthtech.service.SessionManagementService;
import com.thentrees.gymhealthtech.service.UserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${app.prefix}/users/sessions")
@RequiredArgsConstructor
public class SessionController {
  private final SessionManagementService sessionService;
  private final UserService userService;

  // Session lifecycle management endpoints would go here (start, pause, resume, end, etc.)
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
      @Valid @RequestBody CreateStartSessionRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info(
        "POST /users/sessions/start - User {} is starting a session with plan day {}",
        userId,
        request.getPlanDayId());
    SessionResponse session = sessionService.startSession(userId, request);
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
  public ResponseEntity<APIResponse<SessionResponse>> getActiveSession(
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("GET /users/sessions/active - User {} getting active session", userId);

    SessionResponse session = sessionService.getActiveSession(userId);

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
      @Valid @RequestBody CompleteSessionRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("POST /users/sessions/{}/complete - User {} completing session", sessionId, userId);

    SessionResponse session = sessionService.completeSession(userId, sessionId, request);

    return ResponseEntity.ok(APIResponse.success(session));
  }

  @PutMapping("/sets/{sessionSetId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionSetResponse>> updateSessionSet(
      @PathVariable UUID sessionSetId,
      @Valid @RequestBody UpdateSessionSetRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("PUT /users/sessions/sets/{} - User {} updating session set", sessionSetId, userId);

    SessionSetResponse sessionSet = sessionService.updateSessionSet(userId, sessionSetId, request);

    return ResponseEntity.ok(APIResponse.success(sessionSet));
  }

  @PostMapping("/{sessionId}/cancel")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<Void>> cancelSession(
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String reason,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("POST /users/sessions/{}/cancel - User {} cancelling session", sessionId, userId);

    sessionService.cancelSession(userId, sessionId, reason);

    return ResponseEntity.ok(APIResponse.success(null, "Session cancelled successfully"));
  }

  @PostMapping("/{sessionId}/pause")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<Void>> pauseSession(
      @PathVariable UUID sessionId,
      @RequestParam(required = false) String reason,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("POST /users/sessions/{}/pause - User {} pausing session", sessionId, userId);

    sessionService.pauseSession(userId, sessionId, reason);

    return ResponseEntity.ok(APIResponse.success(null, "Session paused successfully"));
  }

  @GetMapping("/{sessionId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<APIResponse<SessionResponse>> getSessionDetails(
      @PathVariable UUID sessionId, @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    log.info("GET /users/sessions/{} - User {} getting session details", sessionId, userId);

    SessionResponse session = sessionService.getSessionDetails(userId, sessionId);

    return ResponseEntity.ok(APIResponse.success(session));
  }
}
