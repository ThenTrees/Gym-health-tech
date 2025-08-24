package com.thentrees.gymhealthtech.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.thentrees.gymhealthtech.constant.ErrorCodes;
import com.thentrees.gymhealthtech.dto.response.ApiError;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.FieldError;
import com.thentrees.gymhealthtech.util.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final String TRACE_ID_HEADER = "X-Trace-ID";
  private static final String REQUEST_ID_ATTRIBUTE = "requestId";
  private final GenerateTraceId generateTraceId;
  private final GetRequestDetails getRequestDetails;
  private final GetClientIp getClientIp;
  private final DetermineHttpStatus determineHttpStatus;
  private final FormatFileSize formatFileSize;

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<APIResponse<Object>> handleBaseException(
      BaseException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Business exception [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details(getRequestDetails.getRequestDetails(request))
            .metadata(ex.getMetadata())
            .traceId(traceId)
            .build();

    HttpStatus status = determineHttpStatus.determineHttpStatus(ex.getErrorCode());

    return ResponseEntity.status(status)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<APIResponse<Object>> handleBusinessException(
      BusinessException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Business logic error [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details(getRequestDetails.getRequestDetails(request))
            .metadata(ex.getMetadata())
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<APIResponse<Object>> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Resource not found [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    Map<String, Object> metadata = new HashMap<>();
    if (ex.getResourceType() != null) {
      metadata.put("resourceType", ex.getResourceType());
    }
    if (ex.getResourceId() != null) {
      metadata.put("resourceId", ex.getResourceId());
    }

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details(getRequestDetails.getRequestDetails(request))
            .metadata(metadata)
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<APIResponse<Object>> handleValidationException(
      ValidationException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Custom validation error [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    List<FieldError> fieldErrors = null;
    if (ex.getFieldErrors() != null) {
      fieldErrors =
          ex.getFieldErrors().entrySet().stream()
              .map(
                  entry ->
                      FieldError.builder()
                          .field(entry.getKey())
                          .message(entry.getValue())
                          .code("INVALID_VALUE")
                          .build()).toList();
    }

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .fieldErrors(fieldErrors)
            .details(getRequestDetails.getRequestDetails(request))
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  // ========================================
  // FITNESS APP SPECIFIC EXCEPTIONS
  // ========================================

  @ExceptionHandler(WorkoutInProgressException.class)
  public ResponseEntity<APIResponse<Object>> handleWorkoutInProgress(
      WorkoutInProgressException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Workout in progress [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details("Another workout session is already active")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<APIResponse<Object>> handleRateLimitExceeded(
      RateLimitExceededException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Rate limit exceeded [{}]: {} - Request: {} {} - IP: {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI(),
        getClientIp.getClientIp(request));

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("retryAfterSeconds", ex.getRetryAfterSeconds());
    metadata.put("limitType", "API_RATE_LIMIT");

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details("Please wait before making another request")
            .metadata(metadata)
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  // ========================================
  // SECURITY EXCEPTIONS
  // ========================================

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<APIResponse<Object>> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    String errorCode = ErrorCodes.UNAUTHORIZED;
    String message = "Authentication failed";

    // Specific authentication error handling
    if (ex instanceof BadCredentialsException) {
      errorCode = ErrorCodes.INVALID_CREDENTIALS;
      message = "Invalid username or password";
    } else if (ex instanceof DisabledException) {
      errorCode = ErrorCodes.ACCOUNT_LOCKED;
      message = "Account is disabled";
    } else if (ex instanceof LockedException) {
      errorCode = ErrorCodes.ACCOUNT_LOCKED;
      message = "Account is locked";
    }

    log.warn(
        "Authentication failed [{}]: {} - Request: {} {} - IP: {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI(),
        getClientIp.getClientIp(request));

    ApiError error =
        ApiError.builder()
            .code(errorCode)
            .message(message)
            .details("Please check your credentials and try again")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(message, error));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<APIResponse<Object>> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Access denied [{}]: {} - Request: {} {} - IP: {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI(),
        getClientIp.getClientIp(request));

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.FORBIDDEN)
            .message("Access denied")
            .details("You don't have permission to access this resource")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Insufficient permissions", error));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<APIResponse<Object>> handleUnauthorizedException(
      UnauthorizedException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Unauthorized access [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .details("Please login to access this resource")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(ex.getMessage(), error));
  }

  // ========================================
  // VALIDATION EXCEPTIONS
  // ========================================

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<APIResponse<Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Validation failed [{}]: {} validation errors - Request: {} {}",
        traceId,
        ex.getBindingResult().getErrorCount(),
        request.getMethod(),
        request.getRequestURI());

    List<FieldError> fieldErrors = new ArrayList<>();
    BindingResult bindingResult = ex.getBindingResult();

    // Field errors
    bindingResult
        .getFieldErrors()
        .forEach(
            error ->
                fieldErrors.add(
                    FieldError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .code(error.getCode())
                        .build()));

    // Global errors
    bindingResult
        .getGlobalErrors()
        .forEach(
            error ->
                fieldErrors.add(
                    FieldError.builder()
                        .field("global")
                        .message(error.getDefaultMessage())
                        .code(error.getCode())
                        .build()));

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.VALIDATION_ERROR)
            .message("Request validation failed")
            .details(String.format("Found %d validation errors", fieldErrors.size()))
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Validation failed", error));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<APIResponse<Object>> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Constraint violation [{}]: {} violations - Request: {} {}",
        traceId,
        ex.getConstraintViolations().size(),
        request.getMethod(),
        request.getRequestURI());

    List<FieldError> fieldErrors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String fieldName = violation.getPropertyPath().toString();
      fieldErrors.add(
          FieldError.builder()
              .field(fieldName)
              .rejectedValue(violation.getInvalidValue())
              .message(violation.getMessage())
              .code("CONSTRAINT_VIOLATION")
              .build());
    }

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.VALIDATION_ERROR)
            .message("Constraint validation failed")
            .fieldErrors(fieldErrors)
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Validation failed", error));
  }

  // ========================================
  // HTTP/REQUEST EXCEPTIONS
  // ========================================

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<APIResponse<Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Method not supported [{}]: {} not supported for {} - Supported: {}",
        traceId,
        ex.getMethod(),
        request.getRequestURI(),
        Arrays.toString(ex.getSupportedMethods()));

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("supportedMethods", ex.getSupportedMethods());
    metadata.put("requestedMethod", ex.getMethod());

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.BAD_REQUEST)
            .message(
                String.format(
                    "HTTP method '%s' is not supported for this endpoint", ex.getMethod()))
            .details(
                String.format("Supported methods: %s", Arrays.toString(ex.getSupportedMethods())))
            .metadata(metadata)
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Method not allowed", error));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<APIResponse<Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Malformed JSON request [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    String userMessage = "Invalid request format";
    String details = "Please check your request body format";

    // Specific JSON parsing error messages
    Throwable cause = ex.getCause();
    if (cause instanceof InvalidFormatException) {
      InvalidFormatException ife = (InvalidFormatException) cause;
      userMessage =
          String.format("Invalid value for field '%s'", ife.getPath().get(0).getFieldName());
      details = String.format("Expected type: %s", ife.getTargetType().getSimpleName());
    } else if (cause instanceof MismatchedInputException) {
      MismatchedInputException mie = (MismatchedInputException) cause;
      if (!mie.getPath().isEmpty()) {
        userMessage =
            String.format("Invalid input for field '%s'", mie.getPath().get(0).getFieldName());
      }
    }

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.BAD_REQUEST)
            .message(userMessage)
            .details(details)
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(userMessage, error));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<APIResponse<Object>> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Missing request parameter [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    FieldError fieldError =
        FieldError.builder()
            .field(ex.getParameterName())
            .message(
                String.format(
                    "Required %s parameter '%s' is missing",
                    ex.getParameterType(), ex.getParameterName()))
            .code("MISSING_PARAMETER")
            .build();

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.BAD_REQUEST)
            .message("Required request parameter is missing")
            .fieldErrors(Collections.singletonList(fieldError))
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Missing required parameter", error));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<APIResponse<Object>> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Type mismatch [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    String message =
        String.format(
            "Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

    FieldError fieldError =
        FieldError.builder()
            .field(ex.getName())
            .rejectedValue(ex.getValue())
            .message(message)
            .code("TYPE_MISMATCH")
            .build();

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.BAD_REQUEST)
            .message("Parameter type mismatch")
            .fieldErrors(Collections.singletonList(fieldError))
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(message, error));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<APIResponse<Object>> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "No handler found [{}]: {} {} - Headers: {}",
        traceId,
        ex.getHttpMethod(),
        ex.getRequestURL(),
        ex.getHeaders());

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.RESOURCE_NOT_FOUND)
            .message("Endpoint not found")
            .details(
                String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()))
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Endpoint not found", error));
  }

  // ========================================
  // DATABASE EXCEPTIONS
  // ========================================

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<APIResponse<Object>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.error(
        "Data integrity violation [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    String userMessage = "Data constraint violation";
    String errorCode = ErrorCodes.BAD_REQUEST;

    // Check for specific constraint violations
    String exceptionMessage = ex.getMessage().toLowerCase();
    if (exceptionMessage.contains("unique") || exceptionMessage.contains("duplicate")) {
      userMessage = "Duplicate entry - this record already exists";
      errorCode = ErrorCodes.EMAIL_ALREADY_EXISTS; // or appropriate duplicate error
    } else if (exceptionMessage.contains("foreign key")) {
      userMessage = "Referenced record does not exist";
    } else if (exceptionMessage.contains("not null")) {
      userMessage = "Required field is missing";
    }

    ApiError error =
        ApiError.builder()
            .code(errorCode)
            .message(userMessage)
            .details("Please check your data and try again")
            .traceId(traceId)
            .build();

    return ResponseEntity.badRequest()
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error(userMessage, error));
  }

  @ExceptionHandler(OptimisticLockException.class)
  public ResponseEntity<APIResponse<Object>> handleOptimisticLockException(
      OptimisticLockException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Optimistic lock exception [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code("OPTIMISTIC_LOCK_EXCEPTION")
            .message("The record has been modified by another user")
            .details("Please refresh and try again")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Record has been modified", error));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<APIResponse<Object>> handleEntityNotFoundException(
      EntityNotFoundException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Entity not found [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.RESOURCE_NOT_FOUND)
            .message("Requested resource not found")
            .details(ex.getMessage())
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Resource not found", error));
  }

  // ========================================
  // FILE UPLOAD EXCEPTIONS
  // ========================================

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<APIResponse<Object>> handleMaxSizeException(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "File size exceeded [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("maxSize", ex.getMaxUploadSize());
    metadata.put("maxSizeFormatted", formatFileSize.formatFileSize(ex.getMaxUploadSize()));

    ApiError error =
        ApiError.builder()
            .code("FILE_SIZE_EXCEEDED")
            .message("File size exceeds the maximum allowed limit")
            .details(
                String.format(
                    "Maximum allowed size: %s",
                    formatFileSize.formatFileSize(ex.getMaxUploadSize())))
            .metadata(metadata)
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("File too large", error));
  }

  // ========================================
  // TIMEOUT EXCEPTIONS
  // ========================================

  @ExceptionHandler({TimeoutException.class, AsyncRequestTimeoutException.class})
  public ResponseEntity<APIResponse<Object>> handleTimeoutException(
      Exception ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.warn(
        "Request timeout [{}]: {} - Request: {} {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI());

    ApiError error =
        ApiError.builder()
            .code("REQUEST_TIMEOUT")
            .message("Request timed out")
            .details("The request took too long to process")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Request timeout", error));
  }

  // ========================================
  // GENERIC EXCEPTION HANDLER
  // ========================================

  @ExceptionHandler(Exception.class)
  public ResponseEntity<APIResponse<Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {

    String traceId = generateTraceId.generate();
    log.error(
        "Unexpected error [{}]: {} - Request: {} {} - User-Agent: {}",
        traceId,
        ex.getMessage(),
        request.getMethod(),
        request.getRequestURI(),
        request.getHeader("User-Agent"),
        ex);

    ApiError error =
        ApiError.builder()
            .code(ErrorCodes.INTERNAL_SERVER_ERROR)
            .message("An unexpected error occurred")
            .details("Please try again later or contact support")
            .traceId(traceId)
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(TRACE_ID_HEADER, traceId)
        .body(APIResponse.error("Internal server error", error));
  }
}
