package com.thentrees.gymhealthtech.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
  @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "JWT refresh token", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg...")
  private String refreshToken;

  @Schema(description = "Token type", example = "Bearer")
  @Builder.Default
  private String tokenType = "Bearer";

  @Schema(description = "Access token expiration time in seconds", example = "3600")
  private long expiresIn;

  @Schema(description = "User information")
  private UserInfo user;

  @Data
  @Builder
  @Schema(description = "User information in authentication response")
  public static class UserInfo {
    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User phone")
    private String phone;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "User role")
    private String role;

    @Schema(description = "User status")
    private String status;

    @Schema(description = "Email verification status")
    private Boolean emailVerified;

    @Schema(description = "Account creation time")
    private LocalDateTime createdAt;
  }
}
