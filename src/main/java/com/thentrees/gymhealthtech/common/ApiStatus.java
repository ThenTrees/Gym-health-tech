package com.thentrees.gymhealthtech.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiStatus {

  // Success Responses
  SUCCESS(HttpStatus.OK, "Request processed successfully"),
  CREATED(HttpStatus.CREATED, "Resource created successfully"),
  NO_CONTENT(HttpStatus.NO_CONTENT, "Request processed successfully with no content"),

  // Client Error Responses
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
  NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
  CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),
  UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed"),
  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"),

  // Server Error Responses
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");

  private final HttpStatus httpStatus;
  private final String message;

  public int getStatusCode() {
    return httpStatus.value();
  }
}
