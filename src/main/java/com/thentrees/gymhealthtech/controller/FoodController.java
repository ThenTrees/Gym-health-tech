package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.FoodRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import com.thentrees.gymhealthtech.service.FoodService;
import java.io.IOException;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/nutrition")
@RequiredArgsConstructor
@Slf4j
public class FoodController {
  private final FoodService foodService;

  @PostMapping(value = "/foods/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
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

  @Operation(
      summary = "Create a new food item",
      description = "Creates a new food item with the provided details."
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Food item created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<FoodResponse>> createFood(
      @Valid @RequestPart("foodRequest") FoodRequest foodRequest,
      @RequestPart("file") MultipartFile file
      ) {
    FoodResponse createdFood = foodService.createFood(foodRequest, file);
    return ResponseEntity.ok(APIResponse.success(createdFood));
  }

  @Operation(
      summary = "Get food item by ID",
      description = "Retrieves the details of a food item by its unique ID."
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Food item retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Food item not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{foodId}")
  public ResponseEntity<APIResponse<FoodResponse>> getFoodById(@PathVariable("foodId") UUID foodId) {
    FoodResponse foodResponse = foodService.getFoodById(foodId);
    return ResponseEntity.ok(APIResponse.success(foodResponse));
  }

  @PutMapping("/{foodId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> updateFood(
      @PathVariable("foodId") UUID foodId,
      @Valid @RequestBody FoodRequest foodRequest) {
    foodService.updateFood(foodId, foodRequest);
    return ResponseEntity.ok(APIResponse.success("Update food Successfully!")); // Placeholder response
  }

  @DeleteMapping("/{foodId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> deleteFood(
      @PathVariable("foodId") UUID foodId) {

    foodService.deleteFoodById(foodId);
    return ResponseEntity.ok(APIResponse.success("Delete food Successfully!"));
  }

  @PostMapping(value = "/{foodId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> uploadFoodImage(
    @PathVariable("foodId") UUID foodId,
    @RequestParam("file") MultipartFile file) throws IOException {

    return ResponseEntity.ok(APIResponse.success(foodService.uploadImage(foodId, file)));

  }



  private boolean isExcelFile(MultipartFile file) {
    String filename = file.getOriginalFilename();
    return filename != null && filename.endsWith(".xlsx");
  }
}
