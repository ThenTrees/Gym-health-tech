package com.thentrees.gymhealthtech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.ForgotPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.ResetPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.request.VerifyOtpRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.service.UserProfileService;
import com.thentrees.gymhealthtech.service.UserService;
import com.thentrees.gymhealthtech.util.ExtractValidationErrors;
import com.thentrees.gymhealthtech.util.S3Util;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${app.prefix}/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for user management")
public class UserController {

  private final UserProfileService userProfileService;
  private final ExtractValidationErrors extractValidationErrors;
  private final ObjectMapper objectMapper;
  private final S3Util s3Util;
  private final UserService userService;

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

  @Operation(
      method = "DELETE",
      summary = "Delete user profile",
      description = "User can delete their own profile")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully deleted user profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
          {
            "message": "Profile deleted successfully"
          }
          """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication is required",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Access denied",
            content = @Content),
      })
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

  @Operation(
      method = "DELETE",
      summary = "Admin delete user profile",
      description = "Admin can delete any user profile by user ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully deleted user profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
          {
            "message": "Profile deleted successfully"
          }
          """))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication is required",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Access denied",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User profile not found",
            content = @Content),
      })
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

  /** Upload user avatar */
  @Operation(
      method = "POST",
      summary = "Upload user avatar",
      description = "Upload an avatar image for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully uploaded avatar",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
            {
              "url": "https://s3.amazonaws.com/bucketname/filename.jpg",
              "filename": "filename.jpg",
              "size": 204800,
              "message": "File uploaded successfully"
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
              "message": "Only image files are allowed"
            }
            """)))
      })
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<UploadResponse>> uploadAvatar(
      @RequestParam("file") MultipartFile file) {
    try {
      String fileUrl = userProfileService.uploadProfileImage(file);

      UploadResponse uploadResponse =
          UploadResponse.builder()
              .url(fileUrl)
              .filename(file.getOriginalFilename())
              .size(file.getSize())
              .message("File uploaded successfully")
              .build();

      return ResponseEntity.ok(APIResponse.success(uploadResponse, "File uploaded successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Failed to upload file: " + e.getMessage()));
    }
  }

  @Operation(
      method = "POST",
      summary = "Forgot password",
      description = "Sends otp request reset password to email for user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OTP code sent to email",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
          {
            "message": "OTP code sent to email"
          }
          """))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation errors or business exceptions",
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
            "message": "Email is required"
          }
          """))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping("/forgot-password")
  public ResponseEntity<APIResponse<String>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    try {
      userService.forgotPassword(request);
      return ResponseEntity.ok(APIResponse.success("OTP code sent to email"));
    } catch (BusinessException e) {
      return ResponseEntity.badRequest().body(APIResponse.error(e.getMessage()));
    }
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<APIResponse<String>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request) {
    log.info("Verify OTP request for email: {}", request.getEmail());
    String response = userService.verifyOtp(request);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<APIResponse<String>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    log.info("Reset password request for email: {}", request.getEmail());
    userService.resetPassword(request);
    return ResponseEntity.status(HttpStatus.OK)
        .body(APIResponse.success("Password reset successfully"));
  }
}
