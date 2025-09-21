package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
  @NotBlank(message = "Email là bắt buộc")
  @Email(message = "Email không hợp lệ")
  private String email;

  @NotBlank(message = "Mật khẩu mới là bắt buộc")
  private String newPassword;

  @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
  private String confirmPassword;
}
