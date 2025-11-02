package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import com.thentrees.gymhealthtech.enums.NotificationType;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.WorkoutReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.thentrees.gymhealthtech.config.NotificationQueueConfig.EXCHANGE_NAME;
import static com.thentrees.gymhealthtech.config.NotificationQueueConfig.ROUTING_KEY;

// publisher
@Service
@Slf4j(topic = "WORKOUT-REMINDER-SERVICE")
@RequiredArgsConstructor
public class WorkoutReminderServiceImpl implements WorkoutReminderService {

  private final UserRepository userRepository;

  @Qualifier("customRabbitTemplate")
  private final AmqpTemplate rabbitTemplate;

  @Override
  public void sendReminders() {
    log.info("sendReminders");
    LocalDate today = LocalDate.now();

    // Lấy tất cả user có lịch tập hôm nay
    List<User> users = userRepository.findUsersWithWorkoutOnDate(today);

    for (User user : users) {
      // gửi notification qua queue
      String title = String.format("{} ơi, tập luyện nào", user.getProfile().getFullName());
      String message = String.format("Hôm nay bạn có buổi tập. Hãy sẵn sàng nhé 💪");
      SendNotificationRequest msg = SendNotificationRequest.builder()
        .userId(user.getId().toString())
        .title(title)
        .body(message)
        .notificationType(NotificationType.SYSTEM.toString())
        .build();
      rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, msg);
    }
  }
}
