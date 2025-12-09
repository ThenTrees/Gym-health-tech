package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  private String id;
  private String email;
  private String fullName;
  private String phone;
  private UserStatus status;
  private Boolean isPremium;
  private String avatarUrl;
}
