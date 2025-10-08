package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.response.ImportFoodResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
  ImportFoodResponse importDataFood(MultipartFile file) throws IOException;
}
