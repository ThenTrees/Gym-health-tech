package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.dto.request.CreateTemplateDayRequest;
import com.thentrees.gymhealthtech.dto.request.CreateTemplateItemRequest;
import com.thentrees.gymhealthtech.dto.request.CreateTemplateRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutDayResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import com.thentrees.gymhealthtech.service.TemplateWorkoutService;
import com.thentrees.gymhealthtech.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.prefix}/templates")
@RequiredArgsConstructor
public class TemplateWorkoutController {

  private final TemplateWorkoutService templateWorkoutService;
  private final UserService userService;

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

  @PostMapping("/{id}/template-day")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<TemplateWorkoutResponse>> addTemplateDayToTemplate(
    @PathVariable("id") UUID templateId,
    @RequestBody CreateTemplateDayRequest request){
    return ResponseEntity.ok(APIResponse.success(templateWorkoutService.addTemplateDayToTemplate(templateId, request)));
  }

  @PostMapping("/{id}/template-item")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<TemplateWorkoutDayResponse>> addTemplateItemToTemplateDay(
    @PathVariable("id") UUID templateDayId,
    @RequestBody CreateTemplateItemRequest request){
    return ResponseEntity.ok(APIResponse.success(templateWorkoutService.addTemplateItemToTemplateDay(templateDayId, request)));
  }

  @DeleteMapping("/{templateItemId}/items")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<String>> deleteTemplateItemFromTemplateDay(
    @PathVariable("templateItemId") UUID templateItemId
  ){
    templateWorkoutService.removeTemplateItem(templateItemId);
    return ResponseEntity.ok(APIResponse.success("Remove template Item successfully!"));
  }

  @DeleteMapping("/{templateDayId}/days")
  public ResponseEntity<APIResponse<String>> deleteTemplateDayFromTemplate(
    @PathVariable("templateDayId") UUID templateDayId
  ){
    templateWorkoutService.removeTemplateDay(templateDayId);
    return ResponseEntity.ok(APIResponse.success("Delete template Day successfully!"));
  }

  //TODO: apply template
  @GetMapping("/{templateId}/apply")
  public ResponseEntity<APIResponse<String>> applyTemplate(
    @AuthenticationPrincipal UserDetails user,
    @PathVariable("templateId") UUID templateId
    ){

    UUID userId = userService.getUserByUsername(user.getUsername()).getId();

    templateWorkoutService.applyTemplateWorkout(userId, templateId);
    return ResponseEntity.ok(APIResponse.success("Apply success!"));
  }
  //TODO: chuyeenr logic them day - item thanh mang
}
