package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import com.thentrees.gymhealthtech.service.FoodService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

  @GetMapping("/foods")
  public ResponseEntity<APIResponse<PagedResponse<FoodResponse>>> getAllFoods(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size,
      @RequestParam(defaultValue = "foodNameVi") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    Pageable pageable =
        PageRequest.of(page, size, Sort.Direction.fromString(sortDirection), sortBy);

    PagedResponse<FoodResponse> foods = foodService.getAllFoods(keyword, pageable);
    return ResponseEntity.ok(APIResponse.success(foods));
  }

  private boolean isExcelFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    return filename != null && filename.endsWith(".xlsx");
  }
}
