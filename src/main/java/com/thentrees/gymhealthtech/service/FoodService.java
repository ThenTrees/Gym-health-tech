package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import java.io.IOException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
  ImportFoodResponse importDataFood(MultipartFile file) throws IOException;

  PagedResponse<FoodResponse> getAllFoods(String keyword, Pageable pageable);
}
