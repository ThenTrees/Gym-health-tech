package com.thentrees.gymhealthtech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.*;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.service.AuthenticationService;
import com.thentrees.gymhealthtech.service.UserRegistrationService;
import com.thentrees.gymhealthtech.util.ExtractValidationErrors;
import com.thentrees.gymhealthtech.util.GetClientIp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.prefix}/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthenticationController {

  private final ExtractValidationErrors extractValidationErrors;
  private final UserRegistrationService userRegistrationService;
  private final AuthenticationService authenticationService;
  private final GetClientIp getClientIpAddress;
  private final ObjectMapper mapper = new ObjectMapper();

  @Operation(
      method = "POST",
      summary = "Register new user account",
      description =
          "Creates a new user account with profile information. Sends email verification upon successful registration.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "User registration information",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = RegisterRequest.class),
                      examples =
                          @ExampleObject(
                              name = "Complete Registration",
                              summary = "Complete user registration example",
                              value =
                                  """
                    {
                      "email": "john.doe@example.com",
                      "phone": "+84901234567",
                      "password": "MySecurePass123!",
                      "confirmPassword": "MySecurePass123!",
                      "fullName": "John Doe",
                      "gender": "MALE",
                      "dateOfBirth": "1990-05-15",
                      "heightCm": 175.50,
                      "weightKg": 70.00,
                      "healthNotes": "No known allergies or medical conditions",
                      "timezone": "Asia/Ho_Chi_Minh",
                      "unitWeight": "kg",
                      "unitLength": "cm"
                    }
                    """))))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples =
                        @ExampleObject(
                            name = "Successful Registration",
                            value =
                                """
                    {
                      "status": "success",
                      "message": "User registered successfully",
                      "data": {
                        "userId": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "john.doe@example.com",
                        "phone": "+84901234567",
                        "fullName": "John Doe",
                        "emailVerified": false,
                        "createdAt": "2024-01-15T10:30:00+07:00",
                        "message": "Account created successfully. Please check your email for verification."
                      }
                    }
                    """))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or business rule violation",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "Validation Error",
                          summary = "Input validation failed",
                          value =
                              """
                        {
                          "status": "error",
                          "message": "Validation failed",
                          "errors": {
                            "email": "Email should be valid",
                            "password": "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character",
                            "fullName": "Full name is required"
                          }
                        }
                        """),
                      @ExampleObject(
                          name = "Business Rule Error",
                          summary = "Business logic validation failed",
                          value =
                              """
                        {
                          "status": "error",
                          "message": "Email already exists",
                          "errors": null
                        }
                        """)
                    }))
      })
  @PostMapping("/register")
  public ResponseEntity<APIResponse<RegisterResponse>> registerUser(
      @Parameter(description = "User registration data details", required = true)
          @Valid
          @RequestBody
          RegisterRequest request,
      BindingResult bindingResult) {
    log.info("Registration attempt for email: {}", request.getEmail());
    try {
      // check for validation errors
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
                    "Validation failed", mapper.convertValue(apiError, ApiError.class)));
      }

      RegisterResponse response = userRegistrationService.registerUser(request);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(APIResponse.success(response, "User registered successfully"));

    } catch (BusinessException e) {
      log.warn("Business rule violation during registration: {}", e.getMessage());
      return ResponseEntity.badRequest().body(APIResponse.error(e.getMessage()));

    } catch (Exception e) {
      log.error("Unexpected error during user registration", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Internal server error occurred"));
    }
  }

  @Operation(
      summary = "Login user",
      description = "Authenticates user and returns JWT access and refresh tokens")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Successful Login",
                            value =
                                """
                    {
                      "status": "success",
                      "message": "Login successful",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg...",
                        "tokenType": "Bearer",
                        "expiresIn": 3600,
                        "user": {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "email": "john.doe@example.com",
                          "phone": "+84901234567",
                          "fullName": "John Doe",
                          "role": "USER",
                          "status": "ACTIVE",
                          "emailVerified": true,
                          "createdAt": "2024-01-15T10:30:00+07:00"
                        }
                      }
                    }
                    """))),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            name = "Invalid Credentials",
                            value =
                                """
                    {
                      "status": "error",
                      "message": "Invalid credentials",
                      "errors": null
                    }
                    """)))
      })
  @PostMapping("/login")
  public ResponseEntity<APIResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      BindingResult bindingResult) {
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
                    "Validation failed", mapper.convertValue(apiError, ApiError.class)));
      }
      String userAgent = httpRequest.getHeader("User-Agent");
      String ipAddress = getClientIpAddress.getClientIp(httpRequest);

      AuthResponse response = authenticationService.authenticate(request, userAgent, ipAddress);

      return ResponseEntity.ok(APIResponse.success(response, "Login successful"));

    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error during login", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(APIResponse.error("Internal server error occurred"));
    }
  }

  @Operation(
      summary = "Refresh access token",
      description = "Generates new access token using refresh token")
  @PostMapping("/refresh")
  public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {

    try {
      String userAgent = httpRequest.getHeader("User-Agent");
      String ipAddress = getClientIpAddress.getClientIp(httpRequest);

      AuthResponse response = authenticationService.refreshToken(request, userAgent, ipAddress);

      return ResponseEntity.ok(APIResponse.success(response, "Token refreshed successfully"));

    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.error(e.getMessage()));
    }
  }

  @Operation(summary = "Logout user", description = "Revokes refresh token and logs out user")
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/logout")
  public ResponseEntity<APIResponse<String>> logout(
      @RequestBody(required = false) LogoutRequest request) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String currentUserEmail = authentication.getName();

      if (request == null) {
        request = new LogoutRequest();
      }

      authenticationService.logout(request, currentUserEmail);

      return ResponseEntity.ok(APIResponse.success("Logout successful"));

    } catch (BusinessException e) {
      return ResponseEntity.badRequest().body(APIResponse.error(e.getMessage()));
    }
  }

  @Operation(
      summary = "Verify email address",
      description = "Verifies user email using verification token")
  @PostMapping("/verify-email")
  public ResponseEntity<APIResponse<String>> verifyEmail(
      @Valid @RequestBody EmailVerificationRequest request) {
    try {
      authenticationService.verifyEmail(request);
      return ResponseEntity.ok(APIResponse.success("Email verified successfully", null));
    } catch (BusinessException e) {
      return ResponseEntity.badRequest().body(APIResponse.error(e.getMessage()));
    }
  }
}
