package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.EquipmentMapper;
import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.repository.EquipmentRepository;
import com.thentrees.gymhealthtech.service.EquipmentService;

import java.util.List;

import com.thentrees.gymhealthtech.service.RedisService;
import com.thentrees.gymhealthtech.util.CacheKeyUtils;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.thentrees.gymhealthtech.constant.ErrorMessages.EQUIPMENT_ALREADY_EXISTS;
import static com.thentrees.gymhealthtech.constant.ErrorMessages.UPLOAD_EQUIPMENT_IMAGE_FAILED;
import static com.thentrees.gymhealthtech.constant.S3Constant.S3_DEVICE_IMAGE_FOLDER;

@Service
@Slf4j(topic = "EQUIPMENT-SERVICE")
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final FileValidator fileValidator;
  private final S3Util s3Util;
  private final RedisService redisService;
  private final CacheKeyUtils cacheKeyUtils;
  private final ObjectMapper objectMapper;
  private final EquipmentMapper equipmentMapper;

  @Override
  public List<EquipmentResponse> getAllEquipment(String name) {
    // 1. Chuẩn hóa tên key
    String safeName = (name == null || name.isBlank()) ? "all" : name.trim().toLowerCase();
    String cacheKey = cacheKeyUtils.buildKey("equipment:search:", safeName);

    // 2. Đọc cache trước
    try {
      Object cached = redisService.get(cacheKey);
      if (cached != null) {
        List<EquipmentResponse> cachedResult =
          objectMapper.convertValue(cached, new TypeReference<List<EquipmentResponse>>() {});
        if (cachedResult != null && !cachedResult.isEmpty()) {
          log.debug("Cache hit for key: {}", cacheKey);
          return cachedResult;
        }
      }
    } catch (Exception e) {
      log.warn("Failed to read from cache: {}", e.getMessage());
    }

    // 3. Lấy từ DB
    List<Equipment> equipments = (name == null || name.isBlank())
      ? equipmentRepository.findAll()
      : equipmentRepository.findByName(name);

    List<EquipmentResponse> result = equipments.stream()
      .map(equipmentMapper::toResponse)
      .toList();

    // 4. Ghi lại cache (nếu có data)
    if (!result.isEmpty()) {
      try {
        redisService.set(cacheKey, result);
        log.debug("Cache stored for key: {}", cacheKey);
      } catch (Exception e) {
        log.warn("Failed to store cache for key {}: {}", cacheKey, e.getMessage());
      }
    }

    return result;
  }

  @Override
  public EquipmentResponse addEquipment(CreateEquipmentRequest request, MultipartFile file) {

    redisService.deletePattern("equipment:*");

    fileValidator.validateImage(file);
    String fileUrl= null;

    Equipment equipmentExist =
        equipmentRepository.findByCode(request.getEquipmentCode()).orElse(null);
    if (equipmentExist != null) {
      throw new BusinessException(EQUIPMENT_ALREADY_EXISTS);
    }
    equipmentExist = Equipment.builder()
      .code(request.getEquipmentCode())
      .name(request.getEquipmentName())
      .build();
    try {
      fileUrl = s3Util.uploadFile(file, S3_DEVICE_IMAGE_FOLDER);
      equipmentExist.setImageUrl(fileUrl);
    } catch (Exception e) {
      log.error(UPLOAD_EQUIPMENT_IMAGE_FAILED + e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException(UPLOAD_EQUIPMENT_IMAGE_FAILED, e.getMessage());
    }
    equipmentRepository.save(equipmentExist);
    return equipmentMapper.toResponse(equipmentExist);
  }

  @Override
  public void uploadImage(String code, MultipartFile file) {

    redisService.deletePattern("equipment:*");

    Equipment equipment = equipmentRepository.findById(code)
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", code));
    fileValidator.validateImage(file);
    String fileUrl = null;
    try {
      fileUrl = s3Util.uploadFile(file, S3_DEVICE_IMAGE_FOLDER);
      equipment.setImageUrl(fileUrl);
      equipmentRepository.save(equipment);
    } catch (Exception e) {
      log.error(UPLOAD_EQUIPMENT_IMAGE_FAILED+e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException(UPLOAD_EQUIPMENT_IMAGE_FAILED, e.getMessage());
    }
  }

  @Override
  public void updateEquipment(String equipmentCode, UpdateEquipmentRequest request) {
    redisService.deletePattern("equipment:*");
    Equipment equipmentExist =
        equipmentRepository
            .findByCode(equipmentCode)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentCode));
    if (!request.getEquipmentName().equals(equipmentExist.getName())) {
      equipmentExist.setName(request.getEquipmentName());
    }
    equipmentRepository.save(equipmentExist);
  }

  @Override
  public void deleteEquipment(String code) {
    redisService.deletePattern("equipment:*");
    Equipment equipment = equipmentRepository.findById(code).orElseThrow(
      ()-> new ResourceNotFoundException("Equipment", code)
    );
      equipmentRepository.delete(equipment);
  }

  @Override
  public EquipmentResponse getEquipment(String code) {
    return equipmentMapper.toResponse(equipmentRepository.findById(code).orElseThrow(()->new ResourceNotFoundException("Equipment", code)));
  }
}
