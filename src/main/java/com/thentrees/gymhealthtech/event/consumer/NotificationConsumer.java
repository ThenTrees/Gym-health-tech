package com.thentrees.gymhealthtech.event.consumer;

import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import com.thentrees.gymhealthtech.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationService notificationService;

  @RabbitListener(queues = "notification.queue")
  public void consume(SendNotificationRequest message) {
    try {
      notificationService.sendPushNotification(message);
    } catch (Exception e) {
      throw new AmqpRejectAndDontRequeueException("Failed to send notification", e);
    }
  }
}
