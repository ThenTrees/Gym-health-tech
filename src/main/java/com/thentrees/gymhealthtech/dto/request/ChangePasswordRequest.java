package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Change password request")
public class ChangePasswordRequest {

  @NotBlank(message = "Current password is required")
  @Schema(description = "Current password", example = "OldPassword123!")
  private String currentPassword;

  @NotBlank(message = "New password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message =
          "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character")
  @Schema(description = "New password", example = "NewPassword123!")
  private String newPassword;

  @NotBlank(message = "Confirm new password is required")
  @Schema(description = "Confirm new password", example = "NewPassword123!")
  private String confirmNewPassword;
}
