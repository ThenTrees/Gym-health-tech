package com.thentrees.gymhealthtech.service;

import io.github.jav.exposerversdk.PushClientException;

public interface WorkoutReminderService {
  void sendReminders() throws PushClientException, InterruptedException;
}
