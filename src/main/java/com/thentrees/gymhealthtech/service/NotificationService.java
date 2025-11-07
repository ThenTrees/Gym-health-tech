package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.PushTokenRequest;
import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import io.github.jav.exposerversdk.PushClientException;
import org.springframework.security.core.Authentication;

public interface NotificationService {
  void savePushToken(PushTokenRequest request, Authentication authentication);

  void sendPushNotification(SendNotificationRequest request)
      throws PushClientException, InterruptedException;

  boolean isUserSubscribed(Authentication authentication, String platform);
}
