package com.thentrees.gymhealthtech.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipmentResponse {
  private String name;
  private String imageUrl;
}
