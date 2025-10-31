package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.model.Food;
import com.thentrees.gymhealthtech.repository.EquipmentRepository;
import com.thentrees.gymhealthtech.service.EquipmentService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.thentrees.gymhealthtech.constant.S3Constant.S3_DEVICE_IMAGE_FOLDER;
import static com.thentrees.gymhealthtech.constant.S3Constant.S3_FOOD_IMAGE_FOLDER;

@Service
@Slf4j(topic = "EQUIPMENT-SERVICE")
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final FileValidator fileValidator;
  private final S3Util s3Util;

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

  @Transactional
  @Override
  public EquipmentResponse addEquipment(CreateEquipmentRequest request, MultipartFile file) {
    log.info("addEquipment start");

    fileValidator.validateImage(file);
    String fileUrl= null;

    Equipment equipmentExist =
        equipmentRepository.findByCode(request.getEquipmentCode()).orElse(null);
    if (equipmentExist != null) {
      throw new BusinessException("Equipment already exists!");
    }
    Equipment equipment = new Equipment();
    equipment.setCode(request.getEquipmentCode());
    equipment.setName(request.getEquipmentName());
    try {
      fileUrl = s3Util.uploadFile(file, S3_DEVICE_IMAGE_FOLDER);
      equipmentExist.setImageUrl(fileUrl);
    } catch (Exception e) {
      log.error("Error uploading profile image", e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException("Failed to upload profile image", e.getMessage());
    }

    equipmentRepository.save(equipment);
    log.info("addEquipment successfully");
    return mapToResponse(equipment);
  }

  @Transactional
  @Override
  public String uploadImage(String code, MultipartFile file) {
    log.info("Upload image food!");
    Equipment equipment = equipmentRepository.findById(code)
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", code));
    fileValidator.validateImage(file);
    String fileUrl = null;
    try {
      fileUrl = s3Util.uploadFile(file, S3_FOOD_IMAGE_FOLDER);
      equipment.setImageUrl(fileUrl);
      return fileUrl;
    } catch (Exception e) {
      log.error("Error uploading profile image", e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException("Failed to upload profile image", e.getMessage());
    }
  }

  @Transactional
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

  @Transactional
  @Override
  public void deleteEquipment(String code) {
    log.info("deleteEquipment: {}", code);
    Equipment equipment = equipmentRepository.findById(code).orElseThrow(
      ()-> new ResourceNotFoundException("Equipment", code)
    );
      equipmentRepository.delete(equipment);
  }

  @Override
  public EquipmentResponse getEquipment(String code) {
    return mapToResponse(equipmentRepository.findById(code).orElseThrow(()->new ResourceNotFoundException("Equipment", code)));
  }

  private EquipmentResponse mapToResponse(Equipment equipment) {
    return EquipmentResponse.builder()
        .name(equipment.getName())
        .imageUrl(equipment.getImageUrl())
        .build();
  }
}
