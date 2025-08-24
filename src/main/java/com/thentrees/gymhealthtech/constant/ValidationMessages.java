package com.thentrees.gymhealthtech.constant;


public final class ValidationMessages {

  // User Validation
  public static final String EMAIL_REQUIRED = "Email is required";
  public static final String EMAIL_INVALID = "Please provide a valid email address";
  public static final String PASSWORD_REQUIRED = "Password is required";
  public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters long";
  public static final String PASSWORD_TOO_WEAK = "Password must contain at least one uppercase letter, one lowercase letter, and one number";

  // Profile Validation
  public static final String FULL_NAME_REQUIRED = "Full name is required";
  public static final String FULL_NAME_TOO_LONG = "Full name cannot exceed 120 characters";
  public static final String INVALID_DATE_OF_BIRTH = "Invalid date of birth";
  public static final String FUTURE_DATE_NOT_ALLOWED = "Date cannot be in the future";
  public static final String AGE_TOO_YOUNG = "You must be at least 13 years old";

  // Measurement Validation
  public static final String WEIGHT_REQUIRED = "Weight is required";
  public static final String WEIGHT_OUT_OF_RANGE = "Weight must be between 20kg and 400kg";
  public static final String HEIGHT_REQUIRED = "Height is required";
  public static final String HEIGHT_OUT_OF_RANGE = "Height must be between 50cm and 250cm";
  public static final String BODYFAT_OUT_OF_RANGE = "Body fat percentage must be between 2% and 70%";

  // Workout Validation
  public static final String EXERCISE_ID_REQUIRED = "Exercise ID is required";
  public static final String SETS_REQUIRED = "Number of sets is required";
  public static final String REPS_REQUIRED = "Number of reps is required";
  public static final String WEIGHT_NEGATIVE = "Weight cannot be negative";
  public static final String REPS_OUT_OF_RANGE = "Reps must be between 1 and 100";
  public static final String SETS_OUT_OF_RANGE = "Sets must be between 1 and 10";

  // Goal Validation
  public static final String OBJECTIVE_REQUIRED = "Fitness objective is required";
  public static final String SESSIONS_PER_WEEK_REQUIRED = "Sessions per week is required";
  public static final String SESSIONS_PER_WEEK_RANGE = "Sessions per week must be between 1 and 14";
  public static final String SESSION_MINUTES_REQUIRED = "Session duration is required";
  public static final String SESSION_MINUTES_RANGE = "Session duration must be between 10 and 180 minutes";

  private ValidationMessages() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
