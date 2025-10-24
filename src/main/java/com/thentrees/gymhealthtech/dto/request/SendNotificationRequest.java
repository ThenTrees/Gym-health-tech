package com.thentrees.gymhealthtech.dto.request;

import lombok.Data;

@Data
public class SendNotificationRequest {
  private String userId;
  private String title;
  private String body;
}
