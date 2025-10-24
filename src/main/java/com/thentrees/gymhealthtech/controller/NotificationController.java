package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.PushTokenRequest;
import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.service.NotificationService;
import com.thentrees.gymhealthtech.service.UserService;
import io.github.jav.exposerversdk.PushClientException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.prefix}/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final UserService userService;
  private final NotificationService notificationService;

  @PostMapping("/push-token")
  public ResponseEntity<APIResponse<String>> savePushToken(
      @RequestBody PushTokenRequest request, @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    notificationService.savePushToken(request, userId);
    return ResponseEntity.ok(APIResponse.success("Push token saved successfully"));
  }

  @PostMapping("/send")
  public ResponseEntity<APIResponse<String>> sendNotification(
      @RequestBody SendNotificationRequest request) {
    try {
      notificationService.sendPushNotification(request);
      return ResponseEntity.ok(APIResponse.success("Notification sent successfully"));
    } catch (PushClientException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
