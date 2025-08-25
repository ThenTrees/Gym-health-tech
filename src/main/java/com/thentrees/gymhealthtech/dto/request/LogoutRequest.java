package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Logout request")
public class LogoutRequest {

  @Schema(description = "Refresh token to revoke", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg...")
  private String refreshToken;

  @Schema(description = "Logout from all devices", example = "false")
  private Boolean logoutFromAllDevices = false;
}
