package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.PushTokenRequest;
import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.service.NotificationService;
import com.thentrees.gymhealthtech.service.UserService;
import io.github.jav.exposerversdk.PushClientException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_V1 + "/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final UserService userService;
  private final NotificationService notificationService;

  @PostMapping("/push-token")
  public ResponseEntity<APIResponse<String>> savePushToken(
    @RequestBody PushTokenRequest request, Authentication authentication) {
    notificationService.savePushToken(request, authentication);
    return ResponseEntity.ok(APIResponse.success(SuccessMessages.PUSH_TOKEN_SUCCESS));
  }

  @PostMapping("/send")
  public ResponseEntity<APIResponse<String>> sendNotification(
      @RequestBody SendNotificationRequest request) {
    try {
      notificationService.sendPushNotification(request);
      return ResponseEntity.ok(APIResponse.success(SuccessMessages.SEND_NOTIFICATION_SUCCESS));
    } catch (PushClientException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Operation(
    summary = "Check if the user is subscribed to notifications on a specific platform",
    description = "Returns true if the user is subscribed, false otherwise."
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription status"),
    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/is-subscribed")
  public ResponseEntity<APIResponse<Boolean>> isUserSubscribed(
    @RequestParam(value = "platform", required = false, defaultValue = "ios") String platform,
      Authentication authentication) {
    boolean isSubscribed = notificationService.isUserSubscribed(authentication,platform);
    return ResponseEntity.ok(APIResponse.success(isSubscribed));
  }
}
