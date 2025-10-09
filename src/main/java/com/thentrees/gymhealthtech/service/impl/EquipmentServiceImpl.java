package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.repository.EquipmentRepository;
import com.thentrees.gymhealthtech.service.EquipmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

  private final EquipmentRepository equipmentRepository;

  @Override
  public List<EquipmentResponse> getAllEquipment(String name) {
    log.info("getAllEquipment");
    if (StringUtils.isEmpty(name)) {
      List<Equipment> equipments = equipmentRepository.findAll();
      return equipments.stream().map(this::mapToResponse).toList();
    } else {
      List<Equipment> equipmentList = equipmentRepository.findByName(name);
      return equipmentList.stream().map(this::mapToResponse).toList();
    }
  }

  @Override
  public EquipmentResponse addEquipment(CreateEquipmentRequest request) {
    log.info("addEquipment start");
    Equipment equipmentExist =
        equipmentRepository.findByCode(request.getEquipmentCode()).orElse(null);
    if (equipmentExist != null) {
      throw new BusinessException("Equipment already exists!");
    }
    Equipment equipment = new Equipment();
    equipment.setCode(request.getEquipmentCode());
    equipment.setName(request.getEquipmentName());
    equipment.setImageUrl(request.getImageUrl());
    equipmentRepository.save(equipment);
    log.info("addEquipment successfully");
    return mapToResponse(equipment);
  }

  @Override
  public void updateEquipment(String equipmentCode, UpdateEquipmentRequest request) {
    log.info("Equipment with code {} start updated", equipmentCode);
    Equipment equipmentExist =
        equipmentRepository
            .findByCode(equipmentCode)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentCode));
    if (request.getEquipmentName() != null) {
      equipmentExist.setName(request.getEquipmentName());
    }
    if (request.getImageUrl() != null) {
      equipmentExist.setImageUrl(request.getImageUrl());
    }
    equipmentRepository.save(equipmentExist);
    log.info("Equipment with code {} updated successfully", equipmentCode);
  }

  private EquipmentResponse mapToResponse(Equipment equipment) {
    return EquipmentResponse.builder()
        .name(equipment.getName())
        .imageUrl(equipment.getImageUrl())
        .build();
  }
}
