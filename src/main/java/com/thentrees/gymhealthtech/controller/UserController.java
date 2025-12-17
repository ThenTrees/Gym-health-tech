package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.ForgotPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.ResetPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.request.VerifyOtpRequest;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.service.UserProfileService;
import com.thentrees.gymhealthtech.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AppConstants.API_V1 + "/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for user management")
public class UserController {

  private final UserProfileService userProfileService;
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
      UserProfileResponse userProfile = userProfileService.getUserProfile();
      return ResponseEntity.ok(APIResponse.success(userProfile, SuccessMessages.GET_PROFILE));

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
      @Valid @RequestBody UpdateProfileRequest request) {
            UserProfileResponse updatedProfile = userProfileService.updateUserProfile(request);
      return ResponseEntity.ok(
          APIResponse.success(updatedProfile, SuccessMessages.PROFILE_UPDATED));
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
      userProfileService.deleteProfile();
      return ResponseEntity.ok(APIResponse.success(null, SuccessMessages.PROFILE_DELETED));
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
      userProfileService.deleteProfile(userId);
      return ResponseEntity.ok(
          APIResponse.success(null, SuccessMessages.PROFILE_DELETED));
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
      userService.forgotPassword(request);
      return ResponseEntity.ok(APIResponse.success("OTP code sent to email"));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<APIResponse<String>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request) {
    String response = userService.verifyOtp(request);
    return ResponseEntity.ok(APIResponse.success(response));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<APIResponse<String>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {

    userService.resetPassword(request);
    return ResponseEntity.status(HttpStatus.OK)
        .body(APIResponse.success("Password reset successfully"));
  }

  @GetMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<APIResponse<PagedResponse<UserResponse>>> getAllUserProfile(
      @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
      @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
      @RequestParam(
              value = "sortDirection",
              defaultValue = AppConstants.DEFAULT_SORT_DIRECTION)
          String sortDirection
  ) {

      PagedResponse<UserResponse> userSummary = userService.getAllUsers(page, size, sortBy, sortDirection);

    return ResponseEntity.ok(APIResponse.success(userSummary, SuccessMessages.GET_PROFILE));
  }
}
