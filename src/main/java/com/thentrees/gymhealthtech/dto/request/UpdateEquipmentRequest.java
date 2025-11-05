package com.thentrees.gymhealthtech.dto.request;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEquipmentRequest {
  @NotBlank(message = ValidationMessages.EQUIPMENT_NAME_REQUIRE)
  private String equipmentName;
}
