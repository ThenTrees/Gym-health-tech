package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Refresh token request")
public class RefreshTokenRequest {

  @NotBlank(message = "Refresh token is required")
  @Schema(description = "Refresh token", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg...")
  private String refreshToken;
}
