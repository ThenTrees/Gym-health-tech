package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEquipmentRequest {
  @NotBlank(message = ValidationMessages.EQUIPMENT_CODE_REQUIRE)
  private String equipmentCode;
  @NotBlank(message = ValidationMessages.EQUIPMENT_NAME_REQUIRE)
  private String equipmentName;
}
