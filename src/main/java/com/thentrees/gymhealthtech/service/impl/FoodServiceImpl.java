package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.FoodImportRequest;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.model.Food;
import com.thentrees.gymhealthtech.repository.FoodRepository;
import com.thentrees.gymhealthtech.service.FoodService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

  private final FoodRepository foodRepository;

  public List<FoodImportRequest> parseExcel(MultipartFile file) throws IOException {
    List<FoodImportRequest> foods = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);

      // Skip header row
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        try {
          FoodImportRequest food = parseRow(row);
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
    // Parse Excel
    List<FoodImportRequest> dtos = parseExcel(file);

    int successCount = 0;
    int failCount = 0;
    List<String> errors = new ArrayList<>();

    for (FoodImportRequest dto : dtos) {
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

  private Food mapToEntity(FoodImportRequest dto) {
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
    food.setImageUrl(dto.getImage());

    // Tags
    food.setTags(Collections.singletonList(dto.getSearchTags()));

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

  private FoodImportRequest parseRow(Row row) {
    FoodImportRequest food = new FoodImportRequest();

    // Column mapping từ Excel
    food.setFoodNameVi(getCellValueAsString(row, 0)); // A
    food.setFoodName(getCellValueAsString(row, 1)); // B
    food.setServingWeightGrams(getCellValueAsBigDecimal(row, 2)); // C
    food.setCalories(getCellValueAsBigDecimal(row, 3)); // D
    food.setProtein(getCellValueAsBigDecimal(row, 4)); // E
    food.setCarbs(getCellValueAsBigDecimal(row, 5)); // F
    food.setFat(getCellValueAsBigDecimal(row, 6)); // G
    food.setImage(getCellValueAsString(row, 7)); // H
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
    food.setSearchTags(getCellValueAsString(row, 19)); // U
    food.setMealTime(getCellValueAsString(row, 20)); // U

    return food;
  }

  //  private String mapMealTime(String mealTime) {
  //    if (mealTime == null) return "breakfast";
  //
  //    // Map Vietnamese to English codes
  //    return switch (mealTime.toLowerCase()) {
  //      case "bữa sáng" -> "breakfast";
  //      case "bữa trưa" -> "lunch";
  //      case "bữa tối" -> "dinner";
  //      case "bữa phụ" -> "snack";
  //      default -> "snack";
  //    };
  //  }

  private String buildEnhancedDescription(FoodImportRequest dto) {
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
