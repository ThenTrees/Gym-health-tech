package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.common.GenderType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateProfileRequest {

  @Size(max = 120)
  private String fullName;

  private GenderType gender;

  private Integer age;

  @DecimalMin(value = "100.0", message = "Height must be at least 100cm")
  @DecimalMax(value = "250.0", message = "Height must be at most 250cm")
  private BigDecimal heightCm;

  @DecimalMin(value = "30.0", message = "Weight must be at least 30kg")
  @DecimalMax(value = "300.0", message = "Weight must be at most 300kg")
  private BigDecimal weightKg;

  private String healthNotes;

  @Size(max = 8)
  private String unitWeight;

  @Size(max = 8)
  private String unitLength;
}
