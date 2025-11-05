package com.thentrees.gymhealthtech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.thentrees.gymhealthtech.constant.ValidationMessages.*;

@Data
@Schema(description = "Change password request")
public class ChangePasswordRequest {

  @NotBlank(message = CURRENT_PASSWORD_REQUIRED)
  @Schema(description = "Current password", example = "OldPassword123!")
  private String currentPassword;

  @NotBlank(message = NEW_PASSWORD_REQUIRED)
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = PASSWORD_TOO_WEAK)
  @Schema(description = "New password", example = "NewPassword123!")
  private String newPassword;

  @NotBlank(message = CONFIRM_PASSWORD_REQUIRED)
  @Schema(description = "Confirm new password", example = "NewPassword123!")
  private String confirmNewPassword;
}
