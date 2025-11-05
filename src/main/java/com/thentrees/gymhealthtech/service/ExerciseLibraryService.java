package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.request.ExerciseSearchRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateExerciseRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface ExerciseLibraryService {
  PagedResponse<ExerciseListResponse> getExercises(ExerciseSearchRequest request);

  ExerciseDetailResponse createExercise(
      CreateExerciseRequest request);

  int importExercisesFromJson(MultipartFile file) throws IOException;

  ExerciseDetailResponse getExerciseById(UUID id);

  List<MuscleResponse> getMuscles();

  void updateExercise(UUID exerciseId, UpdateExerciseRequest request);

  void deleteExercise(UUID exerciseId);
}
