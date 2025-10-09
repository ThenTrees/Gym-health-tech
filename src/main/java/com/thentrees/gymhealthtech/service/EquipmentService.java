package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import java.util.List;

public interface EquipmentService {

  List<EquipmentResponse> getAllEquipment(String name);

  EquipmentResponse addEquipment(CreateEquipmentRequest request);

  void updateEquipment(String equipmentCode, UpdateEquipmentRequest request);
}
