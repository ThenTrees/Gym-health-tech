package com.thentrees.gymhealthtech.dto.response;

import static com.thentrees.gymhealthtech.constant.AppConstants.STATUS_ERROR;
import static com.thentrees.gymhealthtech.constant.AppConstants.STATUS_SUCCESS;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {

  @Schema(description = "Response status", example = "success")
  private String status;

  @Schema(description = "Response message", example = "Operation completed successfully")
  private String message;

  @Schema(description = "Response data")
  private T data;

  @Schema(description = "Error details (if any)")
  private ApiError error;

  private ApiMeta meta;

  private LocalDateTime timestamp;

  // Success response builders
  public static <T> APIResponse<T> success(T data) {
    return APIResponse.<T>builder()
        .status(STATUS_SUCCESS)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> APIResponse<T> success(T data, String message) {
    return APIResponse.<T>builder()
        .status(STATUS_SUCCESS)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> APIResponse<T> success(T data, String message, ApiMeta meta) {
    return APIResponse.<T>builder()
        .status(STATUS_SUCCESS)
        .message(message)
        .data(data)
        .meta(meta)
        .timestamp(LocalDateTime.now())
        .build();
  }

  // Error response builders
  public static <T> APIResponse<T> error(String message) {
    return APIResponse.<T>builder()
        .status(STATUS_ERROR)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> APIResponse<T> error(String message, ApiError error) {
    return APIResponse.<T>builder()
        .status(STATUS_ERROR)
        .message(message)
        .error(error)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> APIResponse<T> error(String message, String errorCode) {
    return APIResponse.<T>builder()
        .status(STATUS_ERROR)
        .message(message)
        .error(ApiError.builder().code(errorCode).build())
        .timestamp(LocalDateTime.now())
        .build();
  }
}
