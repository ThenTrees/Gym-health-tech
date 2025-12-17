package com.thentrees.gymhealthtech.dto.request;

import lombok.Data;

@Data
public class PushTokenRequest {
  private String token;
  private String platform;
}
