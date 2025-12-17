package com.thentrees.gymhealthtech.util;

import com.thentrees.gymhealthtech.constant.ErrorCodes;
import org.springframework.http.HttpStatus;

/**
 * Utility class for resolving HTTP status codes from error codes.
 */
public final class HttpStatusResolver {

  private HttpStatusResolver() {
    // Utility class - prevent instantiation
  }

  /**
   * Resolves the appropriate HTTP status code for a given error code.
   *
   * @param errorCode the error code
   * @return the corresponding HTTP status code
   */
  public static HttpStatus resolve(String errorCode) {
    return switch (errorCode) {
      case ErrorCodes.RESOURCE_NOT_FOUND,
          ErrorCodes.USER_NOT_FOUND,
          ErrorCodes.EXERCISE_NOT_FOUND,
          ErrorCodes.SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ErrorCodes.UNAUTHORIZED, ErrorCodes.TOKEN_EXPIRED, ErrorCodes.TOKEN_INVALID -> HttpStatus
          .UNAUTHORIZED;
      case ErrorCodes.FORBIDDEN, ErrorCodes.ACCOUNT_LOCKED -> HttpStatus.FORBIDDEN;
      case ErrorCodes.EMAIL_ALREADY_EXISTS,
          ErrorCodes.PHONE_ALREADY_EXISTS,
          ErrorCodes.ACTIVE_GOAL_EXISTS,
          ErrorCodes.WORKOUT_IN_PROGRESS -> HttpStatus.CONFLICT;
      case ErrorCodes.SUBSCRIPTION_REQUIRED, ErrorCodes.PAYMENT_REQUIRED -> HttpStatus
          .PAYMENT_REQUIRED;
      case ErrorCodes.RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
      case ErrorCodes.VALIDATION_ERROR,
          ErrorCodes.INVALID_MEASUREMENT_VALUE,
          ErrorCodes.INVALID_SET_DATA -> HttpStatus.BAD_REQUEST;
      case ErrorCodes.SERVICE_UNAVAILABLE, ErrorCodes.AI_SERVICE_UNAVAILABLE -> HttpStatus
          .SERVICE_UNAVAILABLE;
      default -> HttpStatus.BAD_REQUEST;
    };
  }
}

