package com.thentrees.gymhealthtech.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {
  private String id;
  private String username;
  private String avatarUrl;
}

