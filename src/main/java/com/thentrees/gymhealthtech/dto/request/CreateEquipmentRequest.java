package com.thentrees.gymhealthtech.dto.request;

import lombok.Data;

@Data
public class CreateEquipmentRequest {
  private String equipmentCode;
  private String equipmentName;
  private String imageUrl;
}
