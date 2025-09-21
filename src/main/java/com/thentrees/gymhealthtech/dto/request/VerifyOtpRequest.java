package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email not valid")
  private String email;

  @NotBlank(message = "OTP là bắt buộc")
  private String otp;
}
