package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateEquipmentRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.service.EquipmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.prefix}/equipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment", description = "endpoint for equipment manager")
public class EquipmentController {

  private final EquipmentService equipmentService;

  @GetMapping
  public ResponseEntity<APIResponse<List<EquipmentResponse>>> getAllEquipment(
      @RequestParam(required = false) String name) {
    return ResponseEntity.status(200)
        .body(APIResponse.success(equipmentService.getAllEquipment(name)));
  }

  @PostMapping
  public ResponseEntity<APIResponse<EquipmentResponse>> createEquipment(
      @RequestBody CreateEquipmentRequest request) {
    EquipmentResponse equipmentResponse = equipmentService.addEquipment(request);
    return ResponseEntity.status(201).body(APIResponse.success(equipmentResponse));
  }

  @PutMapping("/{equipmentCode}")
  public ResponseEntity<APIResponse<String>> updateEquipment(
      @PathVariable("equipmentCode") String equipmentCode,
      @RequestBody UpdateEquipmentRequest request) {
    equipmentService.updateEquipment(equipmentCode, request);
    return ResponseEntity.status(200).body(APIResponse.success("Update success"));
  }
}
