package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.GenderType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

  private UUID userId;

  private String fullName;

  private GenderType gender;

  private LocalDate dateOfBirth;

  private BigDecimal heightCm;

  private BigDecimal weightKg;

  private BigDecimal bmi;

  private String bmiCategory;

  private String healthNotes;

  @Builder.Default private String timezone = "Asia/Ho_Chi_Minh";

  @Builder.Default private String unitWeight = "kg";

  @Builder.Default private String unitLength = "cm";

  private OffsetDateTime createdAt;

  private OffsetDateTime updatedAt;
}
