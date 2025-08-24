package com.thentrees.gymhealthtech.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean success;
  private String message;
  private T data;
  private ApiError error;
  private ApiMeta meta;
  private OffsetDateTime timestamp;

  // Success response builders
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
      .success(true)
      .data(data)
      .timestamp(OffsetDateTime.now())
      .build();
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
      .success(true)
      .message(message)
      .data(data)
      .timestamp(OffsetDateTime.now())
      .build();
  }

  public static <T> ApiResponse<T> success(T data, String message, ApiMeta meta) {
    return ApiResponse.<T>builder()
      .success(true)
      .message(message)
      .data(data)
      .meta(meta)
      .timestamp(OffsetDateTime.now())
      .build();
  }

  // Error response builders
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
      .success(false)
      .message(message)
      .timestamp(OffsetDateTime.now())
      .build();
  }

  public static <T> ApiResponse<T> error(String message, ApiError error) {
    return ApiResponse.<T>builder()
      .success(false)
      .message(message)
      .error(error)
      .timestamp(OffsetDateTime.now())
      .build();
  }

  public static <T> ApiResponse<T> error(String message, String errorCode) {
    return ApiResponse.<T>builder()
      .success(false)
      .message(message)
      .error(ApiError.builder().code(errorCode).build())
      .timestamp(OffsetDateTime.now())
      .build();
  }
}
