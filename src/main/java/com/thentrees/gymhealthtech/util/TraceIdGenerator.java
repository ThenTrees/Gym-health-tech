package com.thentrees.gymhealthtech.util;

import java.util.UUID;

/**
 * Utility class for generating trace IDs for request tracking.
 */
public final class TraceIdGenerator {

  private TraceIdGenerator() {
    // Utility class - prevent instantiation
  }

  /**
   * Generates a short trace ID (8 characters, uppercase) for request tracking.
   *
   * @return a short trace ID string
   */
  public static String generate() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}

