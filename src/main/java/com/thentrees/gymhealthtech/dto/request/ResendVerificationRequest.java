package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Resend verification email request")
public class ResendVerificationRequest {

  @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
  @Email(message = "Email should be valid")
  @Schema(description = "Email address to resend verification", example = "user@example.com")
  private String email;
}
