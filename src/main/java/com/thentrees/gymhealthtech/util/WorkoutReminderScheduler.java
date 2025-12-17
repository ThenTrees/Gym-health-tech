package com.thentrees.gymhealthtech.util;

import com.thentrees.gymhealthtech.service.WorkoutReminderService;
import io.github.jav.exposerversdk.PushClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkoutReminderScheduler {

  private final WorkoutReminderService reminderService;

  // chạy mỗi ngày lúc 7h sáng
  @Scheduled(cron = "${app.cron.notify-user-schedule}") // có thể phát triển tính năng thông báo theo giờ mà user cài đặt
  public void sendWorkoutReminders() throws PushClientException, InterruptedException {
    reminderService.sendReminders();
  }

  @Scheduled(cron = "${app.cron.notify-breakfast-meal-schedule}")
  public void sendBreakfastReminders() throws PushClientException, InterruptedException {
    reminderService.sendBreakfastReminders();
  }

  @Scheduled(cron = "${app.cron.notify-lunch-meal-schedule}")
  public void sendLunchReminders() throws PushClientException, InterruptedException {
    reminderService.sendLunchReminders();
  }
  @Scheduled(cron = "${app.cron.notify-dinner-meal-schedule}")
  public void sendDinnerReminders() throws PushClientException, InterruptedException {
    reminderService.sendDinnerReminders();
  }
}
