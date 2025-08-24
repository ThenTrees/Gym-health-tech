package com.thentrees.gymhealthtech.constant;

public final class AppConstants {

  // API Versioning
  public static final String API_V1 = "/api/v1";
  public static final String CURRENT_API_VERSION = "1.0";

  // Pagination
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;
  public static final int DEFAULT_PAGE_NUMBER = 0;
  public static final String DEFAULT_SORT_DIRECTION = "DESC";
  public static final String DEFAULT_SORT_BY = "createdAt";

  // Cache TTL (in seconds)
  public static final int CACHE_TTL_SHORT = 300; // 5 minutes
  public static final int CACHE_TTL_MEDIUM = 3600; // 1 hour
  public static final int CACHE_TTL_LONG = 86400; // 24 hours

  // Rate Limiting
  public static final int RATE_LIMIT_LOGIN = 5; // attempts per 15 minutes
  public static final int RATE_LIMIT_API_GENERAL = 1000; // requests per hour
  public static final int RATE_LIMIT_API_PREMIUM = 5000; // requests per hour

  // Security
  public static final int PASSWORD_MIN_LENGTH = 8;
  public static final int PASSWORD_MAX_LENGTH = 128;
  public static final int JWT_EXPIRATION_MINUTES = 15;
  public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;
  public static final int VERIFICATION_TOKEN_EXPIRATION_HOURS = 24;

  // Workout Limits
  public static final int MAX_EXERCISES_PER_SESSION = 20;
  public static final int MAX_SETS_PER_EXERCISE = 10;
  public static final int MAX_SESSION_DURATION_HOURS = 5;
  public static final int MIN_REST_TIME_SECONDS = 10;
  public static final int MAX_REST_TIME_SECONDS = 600;

  // Body Measurements Validation
  public static final double MIN_WEIGHT_KG = 20.0;
  public static final double MAX_WEIGHT_KG = 400.0;
  public static final double MIN_HEIGHT_CM = 50.0;
  public static final double MAX_HEIGHT_CM = 250.0;
  public static final double MIN_BODYFAT_PCT = 2.0;
  public static final double MAX_BODYFAT_PCT = 70.0;

  // File Upload
  public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"};
  public static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/webm", "video/quicktime"};

  // Notification
  public static final int MAX_NOTIFICATION_HISTORY_DAYS = 90;
  public static final int DAILY_NOTIFICATION_LIMIT = 10;

  // Feature Flags
  public static final String FEATURE_AI_COACHING = "ai_coaching";
  public static final String FEATURE_NUTRITION = "nutrition";
  public static final String FEATURE_SOCIAL = "social";
  public static final String FEATURE_ADVANCED_ANALYTICS = "advanced_analytics";

  private AppConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
