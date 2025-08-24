package com.thentrees.gymhealthtech.constant;


public final class ErrorCodes {

  // Generic Error Codes
  public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
  public static final String BAD_REQUEST = "BAD_REQUEST";
  public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
  public static final String UNAUTHORIZED = "UNAUTHORIZED";
  public static final String FORBIDDEN = "FORBIDDEN";
  public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
  public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

  // Authentication & Authorization
  public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
  public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
  public static final String TOKEN_INVALID = "TOKEN_INVALID";
  public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
  public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
  public static final String PASSWORD_RESET_REQUIRED = "PASSWORD_RESET_REQUIRED";

  // User Management
  public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
  public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
  public static final String PHONE_ALREADY_EXISTS = "PHONE_ALREADY_EXISTS";
  public static final String INVALID_USER_STATUS = "INVALID_USER_STATUS";
  public static final String PROFILE_INCOMPLETE = "PROFILE_INCOMPLETE";

  // Exercise & Content
  public static final String EXERCISE_NOT_FOUND = "EXERCISE_NOT_FOUND";
  public static final String EXERCISE_NOT_AVAILABLE = "EXERCISE_NOT_AVAILABLE";
  public static final String CONTENT_ASSET_NOT_FOUND = "CONTENT_ASSET_NOT_FOUND";
  public static final String INVALID_EXERCISE_LEVEL = "INVALID_EXERCISE_LEVEL";

  // Workout & Sessions
  public static final String SESSION_NOT_FOUND = "SESSION_NOT_FOUND";
  public static final String WORKOUT_IN_PROGRESS = "WORKOUT_IN_PROGRESS";
  public static final String NO_ACTIVE_WORKOUT = "NO_ACTIVE_WORKOUT";
  public static final String INVALID_WORKOUT_STATE = "INVALID_WORKOUT_STATE";
  public static final String SESSION_ALREADY_COMPLETED = "SESSION_ALREADY_COMPLETED";
  public static final String INVALID_SET_DATA = "INVALID_SET_DATA";

  // Plans & Goals
  public static final String GOAL_NOT_FOUND = "GOAL_NOT_FOUND";
  public static final String PLAN_NOT_FOUND = "PLAN_NOT_FOUND";
  public static final String PLAN_GENERATION_FAILED = "PLAN_GENERATION_FAILED";
  public static final String ACTIVE_GOAL_EXISTS = "ACTIVE_GOAL_EXISTS";
  public static final String INVALID_PLAN_STATUS = "INVALID_PLAN_STATUS";

  // Measurements & Progress
  public static final String INVALID_MEASUREMENT_VALUE = "INVALID_MEASUREMENT_VALUE";
  public static final String MEASUREMENT_DATE_CONFLICT = "MEASUREMENT_DATE_CONFLICT";
  public static final String INVALID_BODY_METRICS = "INVALID_BODY_METRICS";

  // Subscription & Features
  public static final String SUBSCRIPTION_REQUIRED = "SUBSCRIPTION_REQUIRED";
  public static final String SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";
  public static final String INVALID_SUBSCRIPTION_STATE = "INVALID_SUBSCRIPTION_STATE";
  public static final String FEATURE_NOT_AVAILABLE = "FEATURE_NOT_AVAILABLE";
  public static final String PAYMENT_REQUIRED = "PAYMENT_REQUIRED";

  // Gamification
  public static final String CHALLENGE_NOT_FOUND = "CHALLENGE_NOT_FOUND";
  public static final String CHALLENGE_EXPIRED = "CHALLENGE_EXPIRED";
  public static final String ALREADY_PARTICIPATING = "ALREADY_PARTICIPATING";
  public static final String BADGE_ALREADY_AWARDED = "BADGE_ALREADY_AWARDED";

  // AI & Recommendations
  public static final String AI_SERVICE_UNAVAILABLE = "AI_SERVICE_UNAVAILABLE";
  public static final String RECOMMENDATION_FAILED = "RECOMMENDATION_FAILED";
  public static final String INSUFFICIENT_DATA = "INSUFFICIENT_DATA";

  private ErrorCodes() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
