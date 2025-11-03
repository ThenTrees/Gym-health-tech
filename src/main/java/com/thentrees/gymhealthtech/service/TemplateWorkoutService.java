package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateTemplateRequest;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TemplateWorkoutService {

  TemplateWorkoutResponse createTemplateWorkout(CreateTemplateRequest request, MultipartFile file);
  TemplateWorkoutResponse getTemplateWorkoutById(UUID id);
  List<TemplateWorkoutResponse> getTemplateWorkouts();
  TemplateWorkoutResponse updateTemplateWorkout(UUID id, CreateTemplateRequest request, MultipartFile file);
  void deleteTemplateWorkoutById(UUID id);
  void activeTemplateWorkoutById(UUID id);
}
