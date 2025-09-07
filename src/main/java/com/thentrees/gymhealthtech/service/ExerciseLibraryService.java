package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.response.ExerciseDetailResponse;
import com.thentrees.gymhealthtech.dto.response.ExerciseListResponse;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import org.springframework.security.core.Authentication;

public interface ExerciseLibraryService {
  PagedResponse<ExerciseListResponse> getExercises(ExerciseSearchRequest request);

  ExerciseDetailResponse createExercise(CreateExerciseRequest request, Authentication authentication);
}
