package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.service.FoodService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/nutrition")
@RequiredArgsConstructor
@Slf4j
public class FoodController {
  private final FoodService foodService;

  @PostMapping(value = "/foods/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  //  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ImportFoodResponse> importFoods(@RequestParam("file") MultipartFile file) {
    log.info("Importing foods from Excel: {}", file.getOriginalFilename());

    // Validate file
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(ImportFoodResponse.error("File is empty"));
    }

    if (!isExcelFile(file)) {
      return ResponseEntity.badRequest()
          .body(ImportFoodResponse.error("Only Excel files (.xlsx) are allowed"));
    }

    try {
      ImportFoodResponse result = foodService.importDataFood(file);

      log.info(
          "Import completed: {} success, {} failed out of {} total",
          result.getSuccessCount(),
          result.getFailCount(),
          result.getTotalRows());

      return ResponseEntity.ok(
          ImportFoodResponse.builder()
              .success(true)
              .message(
                  String.format(
                      "Imported %d/%d foods successfully",
                      result.getSuccessCount(), result.getTotalRows()))
              .totalRows(result.getTotalRows())
              .successCount(result.getSuccessCount())
              .failCount(result.getFailCount())
              .errors(result.getErrors())
              .build());

    } catch (IOException e) {
      log.error("Error importing foods", e);
      return ResponseEntity.internalServerError()
          .body(ImportFoodResponse.error("Error reading file: " + e.getMessage()));
    }
  }

  private boolean isExcelFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    return filename != null && filename.endsWith(".xlsx");
  }
}
