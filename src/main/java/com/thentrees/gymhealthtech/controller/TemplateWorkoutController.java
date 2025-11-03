package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateTemplateRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import com.thentrees.gymhealthtech.service.TemplateWorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.prefix}/templates")
@RequiredArgsConstructor
public class TemplateWorkoutController {

  private final TemplateWorkoutService templateWorkoutService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<TemplateWorkoutResponse>> createTemplate(
    @Valid @RequestPart("requestBody") CreateTemplateRequest request,
    @RequestPart("file") MultipartFile file
    ){
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(APIResponse.success(templateWorkoutService.createTemplateWorkout(request, file)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<APIResponse<TemplateWorkoutResponse>> getTemplateWorkout(
    @PathVariable("id") UUID templateId){
    return ResponseEntity.ok(APIResponse.success(templateWorkoutService.getTemplateWorkoutById(templateId)));
  }

  //TODO: response page
  @GetMapping
  public ResponseEntity<APIResponse<List<TemplateWorkoutResponse>>> getTemplates(){
    return ResponseEntity.ok(APIResponse.success(templateWorkoutService.getTemplateWorkouts()));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<TemplateWorkoutResponse>> updateTemplateWorkout(
    @PathVariable("id") UUID templateId,
    @RequestPart("request") CreateTemplateRequest request,
    @RequestPart("file")  MultipartFile file
  ){
    return ResponseEntity.ok(APIResponse.success(templateWorkoutService.updateTemplateWorkout(templateId, request, file)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> deleteTemplateWorkout(
    @PathVariable("id")  UUID templateId
  ){
    templateWorkoutService.deleteTemplateWorkoutById(templateId);
    return ResponseEntity.ok(APIResponse.success("template workout deleted"));
  }

  //TODO: active template
  @GetMapping("/{id}/active")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> isTemplateWorkoutActive(@PathVariable("id") UUID id){
    templateWorkoutService.activeTemplateWorkoutById(id);
    return ResponseEntity.ok(APIResponse.success("Template workout is active."));
  }

  //TODO: add item into template day

  //TODO: add templateDay into temlate
}
