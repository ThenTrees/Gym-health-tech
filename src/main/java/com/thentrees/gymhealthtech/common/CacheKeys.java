package com.thentrees.gymhealthtech.common;

public final class CacheKeys {

  // User Cache Keys
  public static final String USER_PREFIX = "user:";
  public static final String USER_PROFILE_PREFIX = "user:profile:";
  public static final String USER_PREFERENCES_PREFIX = "user:preferences:";

  // Exercise Cache Keys
  public static final String EXERCISE_PREFIX = "exercise:";
  public static final String EXERCISE_LIST_PREFIX = "exercise:list:";
  public static final String MUSCLE_GROUPS = "muscle:groups";
  public static final String EQUIPMENT_TYPES = "equipment:types";

  // Workout Cache Keys
  public static final String SESSION_PREFIX = "session:";
  public static final String ACTIVE_SESSION_PREFIX = "session:active:";
  public static final String PLAN_PREFIX = "plan:";

  // Statistics Cache Keys
  public static final String USER_STATS_PREFIX = "stats:user:";
  public static final String LEADERBOARD_PREFIX = "leaderboard:";

  // Feature Flags Cache Keys
  public static final String FEATURE_FLAGS = "features:flags";
  public static final String USER_ENTITLEMENTS_PREFIX = "user:entitlements:";

  private CacheKeys() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
