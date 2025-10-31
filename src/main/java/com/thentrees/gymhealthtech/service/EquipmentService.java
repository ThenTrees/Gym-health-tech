package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EquipmentService {

  List<EquipmentResponse> getAllEquipment(String name);

  EquipmentResponse addEquipment(CreateEquipmentRequest request, MultipartFile file);

  void updateEquipment(String equipmentCode, UpdateEquipmentRequest request);

  String uploadImage(String code, MultipartFile file);

  void deleteEquipment(String code);

  EquipmentResponse getEquipment(String code);
}
