package com.thentrees.gymhealthtech.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response for user registration")
public class RegisterResponse {
  @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID userId;

  @Schema(description = "User email", example = "user@example.com")
  private String email;

  @Schema(description = "User phone", example = "+84901234567")
  private String phone;

  @Schema(description = "User full name", example = "Nguyen Van A")
  private String fullName;

  @Schema(description = "Email verification status", example = "false")
  private Boolean emailVerified;

  @Schema(description = "Account creation timestamp")
  private LocalDateTime createdAt;

  @Schema(
      description = "Success message",
      example = "Account created successfully. Please check your email for verification.")
  private String message;
}
