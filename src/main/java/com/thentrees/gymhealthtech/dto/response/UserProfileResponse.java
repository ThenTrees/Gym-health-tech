package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.FitnessLevel;
import com.thentrees.gymhealthtech.common.GenderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

  private UUID userId;

  private String fullName;

  private GenderType gender;

  private Integer age;

  private BigDecimal heightCm;

  private BigDecimal weightKg;

  private BigDecimal bmi;

  private String bmiCategory;

  private String healthNotes;

  @Builder.Default private String timezone = "Asia/Ho_Chi_Minh";

  @Builder.Default private String unitWeight = "kg";

  @Builder.Default private String unitLength = "cm";

  private FitnessLevel fitnessLevel;

  private String profileImageUrl;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
