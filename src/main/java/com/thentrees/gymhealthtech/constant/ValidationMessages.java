package com.thentrees.gymhealthtech.constant;

public final class ValidationMessages {

  //AUTH
  public static final String IDENTITY_REQUIRE = "Email or phone is required";

  //TOKEN
  public static final String REFRESH_TOKEN_REQUIRE = "Refresh token is required";
  public static final String TOKEN_REQUIRE = "Token is required";

  //EQUIPMENT VALIDATION
  public static final String EQUIPMENT_CODE_REQUIRE = "Equipment code is required";
  public static final String EQUIPMENT_NAME_REQUIRE = "Equipment name is required";

  //EXERCISE VALIDATION
  public static final String EXERCISE_NAME_REQUIRE = "Exercise name is required";
  public static final String EXERCISE_NAME_EXCEED_120 = "Name must not exceed 120 characters";
  public static final String INTRODUCE_EXCEED_10000 = "Instructions must not exceed 10000 characters";
  public static final String SAFETY_NOTE_EXCEED_5000 = "Safety must not exceed 5000 characters";
  public static final String EXERCISE_CATEGORY_REQUIRE = "Exercise category is required";

  // PLAN VALIDATION
  public static final String PLAN_NAME_REQUIRE = "Name plan is require!";
  public static final String PLAN_NAME_EXCEED_120_CHAR = "Name plan didn't exceed 120 character!";
  public static final String CYCLE_WEEK_REQUIRE = "Cycle week is require!";
  public static final String CYCLE_WEEK_MIN = "The minimum number of training weeks is 1";
  public static final String CYCLE_WEEK_MAX = "The maximum number of training weeks is 52";
  public static final String PLAN_MUST_INCLUDE_PLAN_DAY = "The plan must include at least one day of training.";
  public static final String DAY_IDX_REQUIRE = "Day index is require!";
  public static final String DAY_IDX_MIN = "The minimum daily index is 1";
  public static final String DAY_NAME_EXCEED_50_CHAR = "The division name must not exceed 50 characters";
  public static final String DAY_TRAINING_INCLUDE_EXERCISE = "Each day of training must include at least one exercise.";

  //PLAN_DAY
  public static final String NAME_SLUG_EXCEED_50_CHAR = "name slug do not exceed 50 charactor";

  // EXERCISE
  public static final String EX_ID_REQUIRE = "Exercise ID is require";
  public static final String EX_IDX_REQUIRE = "Ex index is require";
  public static final String EX_IDX_MIN = "Exercise index minimum is 1!";
  public static final String PARAMETER_EXERCISE_REQUIRE = "Parameter exercise is require!";
  public static final String SET_REQUIRE = "Set number is require!";
  public static final String SET_MIN = "Set number at least one";
  public static final String SET_MAX = "Set number maximum at 10";
  public static final String REP_REQUIRE = "Rep number is require!";
  public static final String REST_TIME_NOT_NEGATIVE = "rest time cannot be negative";

  // User Validation
  public static final String EMAIL_REQUIRED = "Email is required";
  public static final String EMAIL_INVALID = "Please provide a valid email address";
  public static final String PASSWORD_REQUIRED = "Password is required";
  public static final String CURRENT_PASSWORD_REQUIRED = "Current password is required";
  public static final String NEW_PASSWORD_REQUIRED = "New password is required";
  public static final String CONFIRM_PASSWORD_REQUIRED = "Confirm password is required";
  public static final String PASSWORD_TOO_SHORT = "Password must be at least 6 characters long";
  public static final String PASSWORD_TOO_WEAK =
      "Password must contain at least one uppercase letter, one lowercase letter, and one number";
  public static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";
  public static final String AGE_REQUIRE = "Age is require";

  // Profile Validation
  public static final String FULL_NAME_REQUIRED = "Full name is required";
  public static final String FULL_NAME_TOO_LONG = "Full name cannot exceed 120 characters";
  public static final String AGE_TOO_YOUNG = "You must be at least 13 years old";

  // Measurement Validation
  public static final String WEIGHT_REQUIRED = "Weight is required";
  public static final String WEIGHT_OUT_OF_RANGE = "Weight must be between 20kg and 250kg";
  public static final String HEIGHT_REQUIRED = "Height is required";
  public static final String HEIGHT_OUT_OF_RANGE = "Height must be between 50cm and 250cm";
  public static final String BODYFAT_OUT_OF_RANGE =
      "Body fat percentage must be between 2% and 70%";
  public static final String HEALTH_NOTE =
    "Health notes must not exceed 1000 characters";

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
  public static final String SESSION_MINUTES_RANGE =
      "Session duration must be between 10 and 180 minutes";

  private ValidationMessages() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
