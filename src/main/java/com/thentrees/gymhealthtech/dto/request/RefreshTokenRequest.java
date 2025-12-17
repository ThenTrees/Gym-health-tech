package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.thentrees.gymhealthtech.constant.ValidationMessages.REFRESH_TOKEN_REQUIRE;

@Data
@Schema(description = "Refresh token request")
public class RefreshTokenRequest {

  @NotBlank(message = REFRESH_TOKEN_REQUIRE)
  @Schema(description = "Refresh token", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg...")
  private String refreshToken;
}
