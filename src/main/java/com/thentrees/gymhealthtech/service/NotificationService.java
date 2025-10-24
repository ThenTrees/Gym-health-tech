package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.PushTokenRequest;
import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import io.github.jav.exposerversdk.PushClientException;
import java.util.UUID;

public interface NotificationService {
  void savePushToken(PushTokenRequest request, UUID userId);

  void sendPushNotification(SendNotificationRequest request)
      throws PushClientException, InterruptedException;
}
