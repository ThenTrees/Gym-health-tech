package com.thentrees.gymhealthtech.constant;

public final class SuccessMessages {

  // User Management
  public static final String USER_CREATED = "User account created successfully";
  public static final String USER_UPDATED = "User profile updated successfully";
  public static final String USER_DELETED = "User account deleted successfully";
  public static final String EMAIL_VERIFIED = "Email verified successfully";
  public static final String PASSWORD_UPDATED = "Password updated successfully";

  // Authentication
  public static final String LOGIN_SUCCESSFUL = "Login successful";
  public static final String LOGOUT_SUCCESSFUL = "Logout successful";
  public static final String TOKEN_REFRESHED = "Token refreshed successfully";
  public static final String PASSWORD_RESET_SENT = "Password reset link sent to your email";
  public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";

  // Workout & Sessions
  public static final String SESSION_STARTED = "Workout session started";
  public static final String SESSION_COMPLETED = "Workout session completed successfully";
  public static final String SESSION_PAUSED = "Workout session paused";
  public static final String SESSION_RESUMED = "Workout session resumed";
  public static final String SET_RECORDED = "Exercise set recorded successfully";

  // Plans & Goals
  public static final String GOAL_CREATED = "Fitness goal created successfully";
  public static final String GOAL_UPDATED = "Fitness goal updated successfully";
  public static final String PLAN_GENERATED = "Workout plan generated successfully";
  public static final String PLAN_UPDATED = "Workout plan updated successfully";

  // Measurements
  public static final String MEASUREMENT_RECORDED = "Body measurement recorded successfully";
  public static final String MEASUREMENT_UPDATED = "Body measurement updated successfully";
  public static final String MEASUREMENT_DELETED = "Body measurement deleted successfully";

  // Subscription
  public static final String SUBSCRIPTION_ACTIVATED = "Subscription activated successfully";
  public static final String SUBSCRIPTION_CANCELLED = "Subscription cancelled successfully";
  public static final String PAYMENT_SUCCESSFUL = "Payment processed successfully";

  // Gamification
  public static final String CHALLENGE_JOINED = "Challenge joined successfully";
  public static final String BADGE_EARNED = "Congratulations! You've earned a new badge";
  public static final String ACHIEVEMENT_UNLOCKED = "Achievement unlocked!";

  private SuccessMessages() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
