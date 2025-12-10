package com.thentrees.gymhealthtech.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting request details from HTTP requests.
 */
public final class RequestDetailsExtractor {

  private RequestDetailsExtractor() {
    // Utility class - prevent instantiation
  }

  /**
   * Extracts request method and URI details from the HTTP request.
   *
   * @param request the HTTP servlet request
   * @return formatted string containing method and URI
   */
  public static String extract(HttpServletRequest request) {
    return String.format("%s %s", request.getMethod(), request.getRequestURI());
  }
}

