package com.thentrees.gymhealthtech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.ApiError;
import com.thentrees.gymhealthtech.dto.response.FieldError;
import com.thentrees.gymhealthtech.dto.response.UserProfileResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.service.UserProfileService;
import com.thentrees.gymhealthtech.util.ExtractValidationErrors;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.prefix}/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for user management")
public class UserController {

  private final UserProfileService userProfileService;
  private final ExtractValidationErrors extractValidationErrors;
  private final ObjectMapper objectMapper;

  @Operation(
      method = "GET",
      summary = "Get profile for user",
      description = "get info of user by id, include roles permissions, and other details")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
          {
            "userId": "123e4567-e89b-12d3-a456-426614174000",
            "fullName": "johndoe",
            "gender": "MALE",
            "dateOfBirth": "1990-01-01",
            "heightCm": 180.5,
            "weightKg": 75.0,
            "bmi": 23.0,
            "healthNotes": "No known allergies",
            "timezone": "Asia/Ho_Chi_Minh",
            "unitWeight": "kg",
            "unitLength": "cm",
            "createdAt": "2023-10-01T12:00:00Z",
            "updatedAt": "2023-10-10T15:30:00Z"
          }
          """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication is required",
            content = @Content),
      })
  @GetMapping("/my-profile")
  public ResponseEntity<APIResponse<UserProfileResponse>> getUserProfile() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      log.info("Authenticated user: {}", authentication.getName()); // email

      UserProfileResponse userProfile = userProfileService.getUserProfile(authentication.getName());

      return ResponseEntity.ok(APIResponse.success(userProfile, SuccessMessages.GET_PROFILE));
    } catch (JwtException ex) {
      log.error("JWT error: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(APIResponse.error("Invalid or expired token"));
    }
  }

  @Operation(method = "PUT", description = "Update user profile", summary = "Update user profile")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated user profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
            {
              "userId": "123e4567-e89b-12d3-a456-426614174000",
              "fullName": "johndoe",)
  }
  """))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
            {
              "code": "VALIDATION_ERROR",
              "fieldErrors": [
                {
                  "field": "fullName",
                  "message": "Full name is required"
                },
                {
                  "field": "dateOfBirth",
                  "message": "Date of birth must be in the past"
                }
              ]
            }
            """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication is required")
      })
  @PatchMapping("/update-profile")
  public ResponseEntity<APIResponse<UserProfileResponse>> updateUserProfile(
      @Valid @RequestBody UpdateProfileRequest request, BindingResult bindingResult) {
    try {
      if (bindingResult.hasErrors()) {
        Map<String, String> errors = extractValidationErrors.extract(bindingResult);
        ApiError apiError =
            ApiError.builder()
                .code("VALIDATION_ERROR")
                .fieldErrors(
                    errors.entrySet().stream()
                        .map(
                            entry ->
                                FieldError.builder()
                                    .field(entry.getKey())
                                    .message(entry.getValue())
                                    .build())
                        .toList())
                .build();
        return ResponseEntity.badRequest()
            .body(
                APIResponse.error(
                    "Validation failed", objectMapper.convertValue(apiError, ApiError.class)));
      }

      UserProfileResponse updatedProfile = userProfileService.updateUserProfile(request);

      return ResponseEntity.ok(
          APIResponse.success(updatedProfile, SuccessMessages.PROFILE_UPDATED));
    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.error(e.getMessage()));
    } catch (Exception ex) {
      log.error("Error updating user profile: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Failed to update profile"));
    }
  }

  @DeleteMapping("/delete-profile")
  public ResponseEntity<APIResponse<Void>> deleteProfile() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      log.info("Authenticated user: {}", authentication.getName()); // email

      String email = authentication.getName();

      userProfileService.deleteProfile();
      log.info("Deleting profile for user with email: {}", email);
      log.info("Profile deleted successfully for user with email: {}", email);

      return ResponseEntity.ok(APIResponse.success(null, SuccessMessages.PROFILE_DELETED));
    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.error(e.getMessage()));
    } catch (Exception ex) {
      log.error("Error deleting user profile: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Failed to delete profile"));
    }
  }

  @DeleteMapping("/admin/delete-profile")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<Void>> deleteProfile(@RequestParam("userId") UUID userId) {
    try {
      userProfileService.deleteProfile(userId);
      log.info("Admin Deleting profile for user with ID: {}", userId);

      return ResponseEntity.ok( // nen tra ve 204 => no content
          APIResponse.success(null, SuccessMessages.PROFILE_DELETED));
    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.error(e.getMessage()));
    } catch (Exception ex) {
      log.error("Error deleting user profile: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Failed to delete profile"));
    }
  }
}
