package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Email verification request")
public class EmailVerificationRequest {

  @NotBlank(message = "Verification token is required")
  @Schema(description = "Email verification token", example = "abc123def456ghi789")
  private String token;
}
