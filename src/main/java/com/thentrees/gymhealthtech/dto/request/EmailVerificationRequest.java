package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.thentrees.gymhealthtech.constant.ValidationMessages.TOKEN_REQUIRE;

@Data
@Schema(description = "Email verification request")
public class EmailVerificationRequest {

  @NotBlank(message = TOKEN_REQUIRE)
  @Schema(description = "Email verification token", example = "abc123def456ghi789")
  private String token;
}
