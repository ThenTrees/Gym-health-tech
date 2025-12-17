package com.thentrees.gymhealthtech.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationRequest {
  private String userId;
  private String title;
  private String body;
  private String notificationType;
}
