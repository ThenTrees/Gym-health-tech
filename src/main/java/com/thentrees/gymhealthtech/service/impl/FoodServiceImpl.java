package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.request.FoodImportRequest;
import com.thentrees.gymhealthtech.dto.request.FoodRequest;
import com.thentrees.gymhealthtech.dto.response.ExerciseListResponse;
import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import com.thentrees.gymhealthtech.enums.UserRole;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.FoodMapper;
import com.thentrees.gymhealthtech.model.Food;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.repository.FoodRepository;
import com.thentrees.gymhealthtech.service.FoodService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.thentrees.gymhealthtech.service.RedisService;
import com.thentrees.gymhealthtech.util.CacheKeyUtils;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.thentrees.gymhealthtech.constant.S3Constant.S3_AVATAR_FOLDER;
import static com.thentrees.gymhealthtech.constant.S3Constant.S3_FOOD_IMAGE_FOLDER;

@Slf4j(topic = "FOOD-SERVICE")
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

  private final FoodRepository foodRepository;
  private final FoodMapper foodMapper;
  private final FileValidator fileValidator;
  private final S3Util s3Util;
  private final CacheKeyUtils cacheKeyUtils;
  private final RedisService redisService;
  private final ObjectMapper objectMapper;

  public List<FoodRequest> parseExcel(MultipartFile file) throws IOException {
    List<FoodRequest> foods = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);

      // Skip header row
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        try {
          FoodRequest food = parseRow(row);
          foods.add(food);
        } catch (Exception e) {
          log.error("Error parsing row {}: {}", i, e.getMessage());
        }
      }
    }

    log.info("Parsed {} foods from Excel", foods.size());
    return foods;
  }

  @Transactional
  @Override
  public ImportFoodResponse importDataFood(MultipartFile file) throws IOException {

    redisService.deletePattern("food:*");

    // Parse Excel
    List<FoodRequest> dtos = parseExcel(file);

    int successCount = 0;
    int failCount = 0;
    List<String> errors = new ArrayList<>();

    for (FoodRequest dto : dtos) {
      try {
        Food food = mapToEntity(dto);
        foodRepository.save(food);
        successCount++;

        log.debug("Imported: {}", dto.getFoodName());
      } catch (Exception e) {
        failCount++;
        errors.add(String.format("%s: %s", dto.getFoodName(), e.getMessage()));
        log.error("Failed to import {}: {}", dto.getFoodName(), e.getMessage());
      }
    }

    return ImportFoodResponse.builder()
        .totalRows(dtos.size())
        .successCount(successCount)
        .failCount(failCount)
        .errors(errors)
        .build();
  }

  @Override
  public PagedResponse<FoodResponse> getAllFoods(String keyword, Pageable pageable) {
    String normalizedKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();
    String cacheKey = "";
    if (user.getRole() == UserRole.USER) {
       cacheKey = cacheKeyUtils.buildKey("food:search:user:", normalizedKeyword);
    }else {
       cacheKey = cacheKeyUtils.buildKey("food:search:admin:", normalizedKeyword);
    }
    // Check cache
    Object cached = redisService.get(cacheKey);
    if (cached != null) {
      PagedResponse<FoodResponse> cachedResponse =
        objectMapper.convertValue(cached, new TypeReference<>() {});
      if (cachedResponse != null) {
        log.debug("Cache HIT for key: {}", cacheKey);
        return cachedResponse;
      }
    }
    Page<Food> foodsPage;
    if (user.getRole() == UserRole.USER) {
      foodsPage = normalizedKeyword.isEmpty()
        ? foodRepository.findAllByIsActiveTrue(pageable)
        : foodRepository.findAllByFoodNameVi(normalizedKeyword, pageable);
    }else{
      foodsPage = normalizedKeyword.isEmpty()
        ? foodRepository.findAll(pageable)
        : foodRepository.findAllWithFoodNameVi(normalizedKeyword, pageable);
    }

    Page<FoodResponse> foodResponses = foodsPage.map(foodMapper::toResponse);
    PagedResponse<FoodResponse> response = PagedResponse.of(foodResponses);

    // Cache result
    redisService.set(cacheKey, response);
    log.debug("Cache SET for key: {}", cacheKey);

    return response;
  }

  @Override
  public FoodResponse createFood(FoodRequest request, MultipartFile file) {
    redisService.deletePattern("food:*");
    fileValidator.validateImage(file);
    String fileUrl = null;
    try {
      fileUrl = s3Util.uploadFile(file, S3_FOOD_IMAGE_FOLDER);
    } catch (Exception e) {
      log.error("Error uploading food image", e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException("Failed to upload food image", e.getMessage());
    }
    Food food = mapToEntity(request);
    food.setImageUrl(fileUrl);
    Food savedFood = foodRepository.save(food);
    return foodMapper.toResponse(savedFood);
  }

  @Override
  public FoodResponse getFoodById(UUID foodId) {
    return
        foodRepository
            .findByIdAndIsActiveTrue(foodId)
            .map(foodMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Food", foodId.toString()));
  }

  @Transactional
  @Override
  public void updateFood(UUID foodId, FoodRequest request) {

    redisService.deletePattern("food:*");

    Food existingFood =
        foodRepository
            .findByIdAndIsActiveTrue(foodId)
            .orElseThrow(() -> new ResourceNotFoundException("Food", foodId.toString()));
    foodMapper.updateFoodFromRequest(request, existingFood);
  }

  @Override
  public void deleteFoodById(UUID foodId) {
    redisService.deletePattern("food:*");
    Food existsFood = foodRepository.findByIdAndIsActiveTrue(foodId).orElseThrow(() -> new ResourceNotFoundException("Food", foodId.toString()));
    existsFood.markAsDeleted();
    existsFood.setIsActive(false);
    foodRepository.save(existsFood);
    log.info("Deleted food has ID: {}", foodId);
  }

  @Override
  public String uploadImage(UUID foodId, MultipartFile file) {

    redisService.deletePattern("food:*");

    Food food = foodRepository.findByIdAndIsActiveTrue(foodId)
      .orElseThrow(() -> new ResourceNotFoundException("Food", foodId.toString()));

    fileValidator.validateImage(file);
    String fileUrl = null;
    try {
      fileUrl = s3Util.uploadFile(file, S3_FOOD_IMAGE_FOLDER);
      food.setImageUrl(fileUrl);
      foodRepository.save(food);
      return fileUrl;
    } catch (Exception e) {
      log.error("Error uploading profile image", e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException("Failed to upload profile image", e.getMessage());
    }
  }

  @Override
  public void makeActiveFood(UUID foodId) {
    Food food = foodRepository.findById(foodId).orElseThrow(
      ()-> new ResourceNotFoundException("Food", foodId.toString())
    );
    food.setIsActive(true);
    food.restore();
    foodRepository.save(food);
    redisService.deletePattern("food:*");
  }


  private Food mapToEntity(FoodRequest dto) {
    Food food = new Food();

    // Basic info
    food.setFoodName(dto.getFoodName());
    food.setFoodNameVi(dto.getFoodNameVi());
    food.setDescription(dto.getDescription());

    // Nutrition
    food.setServingWeightGrams(dto.getServingWeightGrams());
    food.setCalories(dto.getCalories());
    food.setProtein(dto.getProtein());
    food.setCarbs(dto.getCarbs());
    food.setFat(dto.getFat());
    food.setFiber(dto.getFiber());

    // Vitamins
    food.setVitaminA(dto.getVitaminA());
    food.setVitaminC(dto.getVitaminC());
    food.setVitaminD(dto.getVitaminD());

    // Classification
    food.setCategory(dto.getCategory());
    food.setMealTime(dto.getMealTime());
    food.setImageUrl(dto.getImageUrl());

    // Tags
    food.setTags(Collections.singletonList(dto.getTags()));

    // Metadata - Store additional info in JSONB or separate fields
    // For now, put in description
    String enhancedDesc = buildEnhancedDescription(dto);
    food.setDescription(enhancedDesc);
    // Detailed Information (NEW)
    food.setDetailedBenefits(dto.getDetailedBenefits());
    food.setCommonCombinations(dto.getCommonCombinations());
    food.setContraindications(dto.getContraindications());
    food.setAlternativeFoods(dto.getAlternativeFoods());

    food.setIsActive(true);

    return food;
  }

  private FoodRequest parseRow(Row row) {
    FoodRequest food = new FoodRequest();

    // Column mapping từ Excel
    food.setFoodNameVi(getCellValueAsString(row, 0)); // A
    food.setFoodName(getCellValueAsString(row, 1)); // B
    food.setServingWeightGrams(getCellValueAsBigDecimal(row, 2)); // C
    food.setCalories(getCellValueAsBigDecimal(row, 3)); // D
    food.setProtein(getCellValueAsBigDecimal(row, 4)); // E
    food.setCarbs(getCellValueAsBigDecimal(row, 5)); // F
    food.setFat(getCellValueAsBigDecimal(row, 6)); // G
    food.setImageUrl(getCellValueAsString(row, 7)); // H
    food.setMealTime(getCellValueAsString(row, 8)); // I
    food.setVitaminA(getCellValueAsBigDecimal(row, 9)); // J
    food.setVitaminC(getCellValueAsBigDecimal(row, 10)); // K
    food.setVitaminD(getCellValueAsBigDecimal(row, 11)); // L
    food.setCategory(getCellValueAsString(row, 12)); // M
    food.setFiber(getCellValueAsBigDecimal(row, 13)); // N
    food.setDescription(getCellValueAsString(row, 14)); // O
    food.setDetailedBenefits(getCellValueAsString(row, 15)); // P
    food.setCommonCombinations(getCellValueAsString(row, 16)); // R
    food.setContraindications(getCellValueAsString(row, 17)); // S
    food.setAlternativeFoods(getCellValueAsString(row, 18)); // T
    food.setTags(getCellValueAsString(row, 19)); // U
    food.setMealTime(getCellValueAsString(row, 20)); // U

    return food;
  }

  private String buildEnhancedDescription(FoodRequest dto) {
    StringBuilder sb = new StringBuilder();

    if (dto.getDescription() != null) {
      sb.append(dto.getDescription()).append("\n\n");
    }

    if (dto.getDetailedBenefits() != null) {
      sb.append("Lợi ích: ").append(dto.getDetailedBenefits()).append("\n\n");
    }

    if (dto.getCommonCombinations() != null) {
      sb.append("Kết hợp với: ").append(dto.getCommonCombinations()).append("\n\n");
    }

    if (dto.getContraindications() != null) {
      sb.append("Lưu ý: ").append(dto.getContraindications());
    }

    return sb.toString().trim();
  }

  private String getCellValueAsString(Row row, int cellIndex) {
    Cell cell = row.getCell(cellIndex);
    if (cell == null) return null;

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> null;
    };
  }

  private BigDecimal getCellValueAsBigDecimal(Row row, int cellIndex) {
    Cell cell = row.getCell(cellIndex);
    if (cell == null) return null;

    try {
      return switch (cell.getCellType()) {
        case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
        case STRING -> new BigDecimal(cell.getStringCellValue());
        default -> null;
      };
    } catch (Exception e) {
      return null;
    }
  }
}
