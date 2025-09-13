package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request")
public class LoginRequest {

  @NotBlank(message = "Email or phone is required")
  @Schema(description = "Email address or phone number", example = "user@example.com")
  private String identifier;

  @NotBlank(message = "Password is required")
  @Schema(description = "User password", example = "MySecurePass123!")
  private String password;
}
