package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.response.ExerciseDetailResponse;
import com.thentrees.gymhealthtech.dto.response.ExerciseListResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface ExerciseLibraryService {
  PagedResponse<ExerciseListResponse> getExercises(ExerciseSearchRequest request);

  ExerciseDetailResponse createExercise(
      CreateExerciseRequest request, Authentication authentication);

  int importExercisesFromJson(MultipartFile file) throws IOException;

  ExerciseDetailResponse getExerciseById(UUID id);
}
