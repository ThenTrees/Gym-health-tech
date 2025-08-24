package com.thentrees.gymhealthtech.common;


import java.time.format.DateTimeFormatter;

public final class DateTimeConstants {

  // Date Formats
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  public static final String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
  public static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy HH:mm";

  // Date Formatters
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
  public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
  public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);

  // Time Zones
  public static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";
  public static final String UTC_TIMEZONE = "UTC";

  // Duration Constants (in minutes)
  public static final int SHORT_WORKOUT_DURATION = 30;
  public static final int MEDIUM_WORKOUT_DURATION = 60;
  public static final int LONG_WORKOUT_DURATION = 90;

  private DateTimeConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
