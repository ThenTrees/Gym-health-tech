package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.FoodRequest;
import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
  ImportFoodResponse importDataFood(MultipartFile file) throws IOException;

  PagedResponse<FoodResponse> getAllFoods(String keyword, Pageable pageable);

  FoodResponse createFood(FoodRequest request, MultipartFile file);

  FoodResponse getFoodById(UUID foodId);

  void updateFood(UUID foodId, FoodRequest request);

  void deleteFoodById(UUID foodId);

  String uploadImage(UUID foodId, MultipartFile file) throws IOException;
  void makeActiveFood(UUID foodId);
}
