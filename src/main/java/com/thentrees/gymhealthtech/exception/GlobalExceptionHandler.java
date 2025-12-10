package com.thentrees.gymhealthtech.exception;

import com.thentrees.gymhealthtech.constant.ErrorCodes;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.ApiError;
import com.thentrees.gymhealthtech.dto.response.FieldError;
import com.thentrees.gymhealthtech.util.ClientIpExtractor;
import com.thentrees.gymhealthtech.util.FileSizeFormatter;
import com.thentrees.gymhealthtech.util.HttpStatusResolver;
import com.thentrees.gymhealthtech.util.RequestDetailsExtractor;
import com.thentrees.gymhealthtech.util.TraceIdGenerator;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j(topic = "GLOBAL-EXCEPTION")
public class GlobalExceptionHandler {

  private static final String TRACE_ID_HEADER = "X-Trace-ID";
  private static final String TRACE_ID = "traceId";

  // =============================
  // COMMON LOGIC
  // =============================
  private String getOrCreateTraceId() {
    String traceId = MDC.get(TRACE_ID);
    if (traceId == null) {
      traceId = TraceIdGenerator.generate();
      MDC.put(TRACE_ID, traceId);
    }
    return traceId;
  }

  private ResponseEntity<APIResponse<Object>> buildErrorResponse(
    String message,
    String code,
    Object details,
    Map<String, Object> metadata,
    HttpStatus status) {

    String traceId = getOrCreateTraceId();
    ApiError error = ApiError.builder()
      .code(code)
      .message(message)
      .details((String) details)
      .metadata(metadata)
      .traceId(traceId)
      .build();

    return ResponseEntity.status(status)
      .header(TRACE_ID_HEADER, traceId)
      .body(APIResponse.error(message, error));
  }

  private ResponseEntity<APIResponse<Object>> handleAndLog(
    Exception ex,
    HttpServletRequest request,
    String logPrefix,
    HttpStatus status,
    String errorCode,
    Object details,
    Map<String, Object> metadata) {

    String traceId = getOrCreateTraceId();
    log.warn("{} [{}] - Request: {} {}",
      logPrefix, traceId, request.getMethod(), request.getRequestURI(), ex);

    return buildErrorResponse(ex.getMessage(), errorCode, details, metadata, status);
  }

  private ResponseEntity<APIResponse<Object>> handleAndLog(
    String message,
    HttpServletRequest request,
    String logPrefix,
    HttpStatus status,
    String errorCode,
    Object details,
    Map<String, Object> metadata) {

    String traceId = getOrCreateTraceId();
    log.warn("{} [{}] - Request: {} {}",
      logPrefix, traceId, request.getMethod(), request.getRequestURI());

    return buildErrorResponse(message, errorCode, details, metadata, status);
  }

  private ResponseEntity<APIResponse<Object>> handleFieldErrors(
    Exception ex,
    HttpServletRequest request,
    String logPrefix,
    HttpStatus status,
    String errorCode,
    List<FieldError> fieldErrors) {

    Map<String, Object> metadata = fieldErrors != null ? Map.of("fieldErrors", fieldErrors) : null;
    return handleAndLog(ex.getMessage(), request, logPrefix, status, errorCode, RequestDetailsExtractor.extract(request), metadata);
  }

  // =============================
  // BUSINESS EXCEPTIONS
  // =============================
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<APIResponse<Object>> handleBaseException(BaseException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatusResolver.resolve(ex.getErrorCode());
    return handleAndLog(ex, request, "Business exception", status, ex.getErrorCode(),
      RequestDetailsExtractor.extract(request), ex.getMetadata());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<APIResponse<Object>> handleBusinessException(BusinessException ex, HttpServletRequest request) {
    return handleAndLog(ex, request, "Business logic error", HttpStatus.BAD_REQUEST, ex.getErrorCode(),
      RequestDetailsExtractor.extract(request), ex.getMetadata());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<APIResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    Map<String, Object> metadata = new HashMap<>();
    if (ex.getResourceType() != null) metadata.put("resourceType", ex.getResourceType());
    if (ex.getResourceId() != null) metadata.put("resourceId", ex.getResourceId());
    String prefixMsg = "Resource not found: %s with id %s";
    return handleAndLog(ex.getMessage(), request, String.format(prefixMsg, ex.getResourceType(), ex.getResourceId()), HttpStatus.NOT_FOUND, ex.getErrorCode(),
      RequestDetailsExtractor.extract(request), metadata);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<APIResponse<Object>> handleValidationException(ValidationException ex, HttpServletRequest request) {
    List<FieldError> fieldErrors = ex.getFieldErrors() == null ? null :
      ex.getFieldErrors().entrySet().stream()
        .map(e -> FieldError.builder().field(e.getKey()).message(e.getValue()).code("INVALID_VALUE").build())
        .toList();
    return handleFieldErrors(ex, request, "Validation exception", HttpStatus.BAD_REQUEST, ex.getErrorCode(), fieldErrors);
  }

  // =============================
  // SECURITY EXCEPTIONS
  // =============================
  @ExceptionHandler({AuthenticationException.class, TokenExpiredException.class})
  public ResponseEntity<APIResponse<Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
    String errorCode = ErrorCodes.UNAUTHORIZED;
    String message = "Authentication failed";

    if (ex instanceof BadCredentialsException) {
      errorCode = ErrorCodes.INVALID_CREDENTIALS;
      message = "Invalid username or password";
    } else if (ex instanceof DisabledException || ex instanceof LockedException) {
      errorCode = ErrorCodes.ACCOUNT_LOCKED;
      message = "Account is disabled or locked";
    }

    String traceId = getOrCreateTraceId();
    log.warn("Authentication failed [{}] - Request: {} {} - IP: {}", traceId, request.getMethod(),
      request.getRequestURI(), ClientIpExtractor.extract(request), ex);

    ApiError error = ApiError.builder()
      .code(errorCode)
      .message(message)
      .details("Please check your credentials and try again")
      .traceId(traceId)
      .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .header(TRACE_ID_HEADER, traceId)
      .body(APIResponse.error(message, error));
  }

  @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
  public ResponseEntity<APIResponse<Object>> handleAccessDenied(Exception ex, HttpServletRequest request) {
    return handleAndLog(ex.getMessage(), request, "Access denied", HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
      "You don't have permission to access this resource", null);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<APIResponse<Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
    return handleAndLog(ex, request, "Unauthorized access", HttpStatus.UNAUTHORIZED, ex.getErrorCode(),
      "Please login to access this resource", null);
  }

  // =============================
  // VALIDATION EXCEPTIONS
  // =============================
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<APIResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
      .map(f -> FieldError.builder()
        .field(f.getField())
        .rejectedValue(f.getRejectedValue())
        .message(f.getDefaultMessage())
        .code(f.getCode())
        .build())
      .toList();
    return handleFieldErrors(ex, request, "Method argument validation failed", HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, fieldErrors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<APIResponse<Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
    List<FieldError> fieldErrors = ex.getConstraintViolations().stream()
      .map(v -> FieldError.builder()
        .field(v.getPropertyPath().toString())
        .rejectedValue(v.getInvalidValue())
        .message(v.getMessage())
        .code("CONSTRAINT_VIOLATION")
        .build())
      .toList();
    return handleFieldErrors(ex, request, "Constraint violations", HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, fieldErrors);
  }

  // =============================
  // DATABASE EXCEPTIONS
  // =============================
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<APIResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
    String message = "Data constraint violation";
    String code = ErrorCodes.BAD_REQUEST;
    String lowerMsg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
    if (lowerMsg.contains("unique") || lowerMsg.contains("duplicate")) {
      message = "Duplicate entry";
      code = ErrorCodes.EMAIL_ALREADY_EXISTS;
    } else if (lowerMsg.contains("foreign key")) {
      message = "Referenced record does not exist";
    } else if (lowerMsg.contains("not null")) {
      message = "Required field is missing";
    }
    return handleAndLog(ex, request, "Data integrity violation", HttpStatus.BAD_REQUEST, code, message, null);
  }

  @ExceptionHandler({OptimisticLockException.class})
  public ResponseEntity<APIResponse<Object>> handleOptimisticLock(OptimisticLockException ex, HttpServletRequest request) {
    return handleAndLog(ex, request, "Optimistic lock exception", HttpStatus.CONFLICT, "OPTIMISTIC_LOCK_EXCEPTION",
      "The record has been modified by another user. Please refresh and try again", null);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<APIResponse<Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
    return handleAndLog(ex, request, "Entity not found", HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, ex.getMessage(), null);
  }

  @ExceptionHandler({RedisConnectionFailureException.class, RedisCommandTimeoutException.class, InfrastructureException.class})
  public ResponseEntity<APIResponse<Object>> handleRedisErrors(Exception ex, HttpServletRequest request) {
    return handleAndLog(
      ex,
      request,
      "Redis infrastructure error",
      HttpStatus.SERVICE_UNAVAILABLE,
      ErrorCodes.INFRA_REDIS_ERROR,
      "Redis operation failed. Please try again later.",
      null
    );
  }

  @ExceptionHandler(WebClientRequestException.class)
  public ResponseEntity<APIResponse<Object>> handleWebClientRequestError(WebClientRequestException ex, HttpServletRequest request) {
    return handleAndLog(
      ex,
      request,
      "WebClient connection error",
      HttpStatus.SERVICE_UNAVAILABLE,
      ErrorCodes.INFRA_WEBCLIENT_ERROR,
      ex.getMessage(),
      null
    );
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<APIResponse<Object>> handleWebClientResponseError(WebClientResponseException ex, HttpServletRequest request) {
    HttpStatus http = HttpStatus.resolve(ex.getRawStatusCode());
    if (http == null) http = HttpStatus.BAD_GATEWAY;

    String code = ErrorCodes.INFRA_WEBCLIENT_ERROR;

    if (http == HttpStatus.TOO_MANY_REQUESTS) {
      code = ErrorCodes.INFRA_RATE_LIMIT;
    }

    return handleAndLog(
      ex,
      request,
      "WebClient downstream response error",
      http,
      code,
      ex.getResponseBodyAsString(),
      null
    );
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<APIResponse<Object>> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
    return handleAndLog(
      ex,
      request,
      "Rate limit exceeded",
      HttpStatus.TOO_MANY_REQUESTS,
      ErrorCodes.INFRA_RATE_LIMIT,
      "Too many requests. Please slow down.",
      null
    );
  }

  @ExceptionHandler({
    ConnectException.class,
    UnknownHostException.class,
    SocketTimeoutException.class
  })
  public ResponseEntity<APIResponse<Object>> handleServiceUnavailable(Exception ex, HttpServletRequest request) {
    return handleAndLog(
      ex,
      request,
      "External service unavailable",
      HttpStatus.SERVICE_UNAVAILABLE,
      ErrorCodes.INFRA_SERVICE_UNAVAILABLE,
      "The external service is unreachable",
      null
    );
  }

  @ExceptionHandler({IOException.class})
  public ResponseEntity<APIResponse<Object>> handleIOException(IOException ex, HttpServletRequest request) {
    return handleAndLog(
      ex,
      request,
      "I/O error",
      HttpStatus.INTERNAL_SERVER_ERROR,
      ErrorCodes.INFRA_IO_ERROR,
      "An I/O error occurred while processing the request",
      null
    );
  }


  // =============================
  // FILE UPLOAD EXCEPTIONS
  // =============================
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<APIResponse<Object>> handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest request) {
    Map<String, Object> metadata = Map.of(
      "maxSize", ex.getMaxUploadSize(),
      "maxSizeFormatted", FileSizeFormatter.format(ex.getMaxUploadSize())
    );
    return handleAndLog(ex, request, "File size exceeded", HttpStatus.PAYLOAD_TOO_LARGE, "FILE_SIZE_EXCEEDED",
      "Maximum allowed size: " + FileSizeFormatter.format(ex.getMaxUploadSize()), metadata);
  }

  // =============================
  // TIMEOUT
  // =============================
  @ExceptionHandler({TimeoutException.class, AsyncRequestTimeoutException.class})
  public ResponseEntity<APIResponse<Object>> handleTimeout(Exception ex, HttpServletRequest request) {
    return handleAndLog(ex, request, "Request timeout", HttpStatus.REQUEST_TIMEOUT, "REQUEST_TIMEOUT",
      "The request took too long to process", null);
  }

  // =============================
  // HTTP/REQUEST EXCEPTIONS
  // =============================
  @ExceptionHandler({
    HttpRequestMethodNotSupportedException.class,
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    NoHandlerFoundException.class
  })
  public ResponseEntity<APIResponse<Object>> handleHttpExceptions(Exception ex, HttpServletRequest request) {
    String logPrefix = "HTTP/Request error";
    String code = ErrorCodes.BAD_REQUEST;
    String details = RequestDetailsExtractor.extract(request);
    if (ex instanceof HttpRequestMethodNotSupportedException hmnse) {
      code = ErrorCodes.BAD_REQUEST;
      details = "Supported methods: " + Arrays.toString(hmnse.getSupportedMethods());
      return handleAndLog(ex, request, "Method not supported", HttpStatus.METHOD_NOT_ALLOWED, code, details, null);
    } else if (ex instanceof HttpMessageNotReadableException) {
      return handleAndLog(ex, request, "Malformed JSON", HttpStatus.BAD_REQUEST, code,
        "Invalid JSON format", null);
    } else if (ex instanceof MissingServletRequestParameterException msrpe) {
      FieldError fe = FieldError.builder()
        .field(msrpe.getParameterName())
        .message(String.format("Required %s parameter '%s' is missing", msrpe.getParameterType(), msrpe.getParameterName()))
        .code("MISSING_PARAMETER")
        .build();
      return buildErrorResponse("Missing required parameter", code, null, Map.of("fieldErrors", List.of(fe)), HttpStatus.BAD_REQUEST);
    } else if (ex instanceof MethodArgumentTypeMismatchException mate) {
      FieldError fe = FieldError.builder()
        .field(mate.getName())
        .rejectedValue(mate.getValue())
        .message(String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", mate.getValue(), mate.getName(), mate.getRequiredType().getSimpleName()))
        .code("TYPE_MISMATCH")
        .build();
      return buildErrorResponse("Parameter type mismatch", code, null, Map.of("fieldErrors", List.of(fe)), HttpStatus.BAD_REQUEST);
    } else if (ex instanceof NoHandlerFoundException nhfe) {
      return handleAndLog(ex, request, "No handler found", HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND,
        String.format("No handler found for %s %s", nhfe.getHttpMethod(), nhfe.getRequestURL()), null);
    }
    return handleAndLog(ex, request, logPrefix, HttpStatus.BAD_REQUEST, code, details, null);
  }

  // =============================
  // GENERIC
  // =============================
  @ExceptionHandler(Exception.class)
  public ResponseEntity<APIResponse<Object>> handleGeneric(Exception ex, HttpServletRequest request) {
    String traceId = getOrCreateTraceId();
    log.error("Unexpected error [{}]: {} - Request: {} {} - User-Agent: {}",
      traceId, ex.getMessage(), request.getMethod(), request.getRequestURI(), request.getHeader("User-Agent"), ex);

    return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(),
      ErrorCodes.INTERNAL_SERVER_ERROR,
      "Please contact support if the issue persists",
      null,
      HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
