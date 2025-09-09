package com.thentrees.gymhealthtech.dto.request;

import static com.thentrees.gymhealthtech.constant.ValidationMessages.*;

import com.thentrees.gymhealthtech.common.FitnessLevel;
import com.thentrees.gymhealthtech.common.GenderType;
import com.thentrees.gymhealthtech.custom.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** DTO for user registration requests. follow the `Fitbod` app registration */
@Data
@Schema(description = "Request for user registration")
public class RegisterRequest {

  @NotBlank(message = EMAIL_REQUIRED)
  @Email(message = EMAIL_INVALID)
  @Schema(description = "User's email address", example = "user@example.com")
  private String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
  @Schema(description = "User's phone number", example = "+84901234567")
  private String phone;

  @NotBlank(message = PASSWORD_REQUIRED)
  @StrongPassword
  @Schema(description = "User password", example = "MySecurePass123!")
  private String password;

  @NotBlank(message = "Confirm password is required")
  @Schema(description = "Password confirmation", example = "MySecurePass123!")
  private String confirmPassword;

  @NotBlank(message = FULL_NAME_REQUIRED)
  @Size(max = 120, message = FULL_NAME_TOO_LONG)
  @Schema(description = "User full name", example = "Nguyen Van A")
  private String fullName;

  @Schema(description = "User gender", example = "MALE")
  private GenderType gender;

  @Past(message = FUTURE_DATE_NOT_ALLOWED)
  @Schema(description = "User date of birth", example = "1990-01-01")
  private LocalDate dateOfBirth;

  @NotNull(message = HEIGHT_REQUIRED)
  @DecimalMin(value = "50.0", message = HEIGHT_OUT_OF_RANGE)
  @DecimalMax(value = "250.0", message = HEIGHT_OUT_OF_RANGE)
  @Digits(integer = 3, fraction = 2, message = "Height format is invalid")
  @Schema(description = "User height in centimeters", example = "175.50")
  private BigDecimal heightCm;

  @NotNull(message = WEIGHT_REQUIRED)
  @DecimalMin(value = "20.0", message = WEIGHT_OUT_OF_RANGE)
  @DecimalMax(value = "250.0", message = WEIGHT_OUT_OF_RANGE)
  @Digits(integer = 3, fraction = 2, message = "Weight format is invalid")
  @Schema(description = "User weight in kilograms", example = "70.50")
  private BigDecimal weightKg;

  @Size(max = 1000, message = "Health notes must not exceed 1000 characters")
  @Schema(description = "User health notes", example = "No known allergies")
  private String healthNotes;

  @NotBlank(message = "Timezone is required")
  @Schema(description = "User timezone", example = "Asia/Ho_Chi_Minh")
  private String timezone;

  @Schema(description = "Weight unit preference", example = "kg")
  private String unitWeight = "kg";

  @Schema(description = "Length unit preference", example = "cm")
  private String unitLength = "cm";

  @Schema(description = "")
  private FitnessLevel fitnessLevel;
}
