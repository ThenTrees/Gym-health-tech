package com.thentrees.gymhealthtech.service;

import io.github.jav.exposerversdk.PushClientException;

public interface WorkoutReminderService {
  void sendReminders() throws PushClientException, InterruptedException;
  void sendBreakfastReminders() throws PushClientException, InterruptedException;
  void sendLunchReminders() throws PushClientException, InterruptedException;
  void sendDinnerReminders() throws PushClientException, InterruptedException;
}
