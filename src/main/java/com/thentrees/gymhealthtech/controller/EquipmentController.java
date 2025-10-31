package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.service.EquipmentService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${app.prefix}/equipments")
@RequiredArgsConstructor
@Slf4j(topic = "EQUIPMENT-CONTROLLER")
@Tag(name = "Equipment", description = "endpoint for equipment manager")
public class EquipmentController {

  private final EquipmentService equipmentService;

  @GetMapping
  public ResponseEntity<APIResponse<List<EquipmentResponse>>> getAllEquipment(
      @RequestParam(required = false) String name) {
    return ResponseEntity.status(200)
        .body(APIResponse.success(equipmentService.getAllEquipment(name)));
  }

  @GetMapping("/{code}")
  public ResponseEntity<APIResponse<EquipmentResponse>> getEquipmentByCode(
    @PathVariable("code") String code
  ){
    return ResponseEntity.ok(APIResponse.success(equipmentService.getEquipment(code)));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<EquipmentResponse>> createEquipment(
      @Valid @RequestPart("request") CreateEquipmentRequest request,
      @RequestPart("file")MultipartFile file) {
    EquipmentResponse equipmentResponse = equipmentService.addEquipment(request, file);
    return ResponseEntity.status(201).body(APIResponse.success(equipmentResponse));
  }

  @PostMapping(value = "/{code}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> uploadEquipmentImage(
    @PathVariable("code") String code,
    @RequestParam("file") MultipartFile file) {
    equipmentService.uploadImage(code, file);
    return ResponseEntity.ok(APIResponse.success("Upload image for equipment successfully"));
  }

  @PutMapping("/{equipmentCode}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> updateEquipment(
      @PathVariable("equipmentCode") String equipmentCode,
      @RequestBody UpdateEquipmentRequest request) {
    equipmentService.updateEquipment(equipmentCode, request);
    return ResponseEntity.status(200).body(APIResponse.success("Update success"));
  }

  @DeleteMapping("/{equipmentCode}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> deleteEquipment(
    @PathVariable("equipmentCode") String equipmentCode) {
    equipmentService.deleteEquipment(equipmentCode);
    return ResponseEntity.status(200).body(APIResponse.success("Del success"));
  }
}
