package com.thentrees.gymhealthtech.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting client IP address from HTTP requests.
 * Handles various proxy headers (X-Forwarded-For, X-Real-IP).
 */
public final class ClientIpExtractor {

  private ClientIpExtractor() {
    // Utility class - prevent instantiation
  }

  /**
   * Extracts the client IP address from the HTTP request.
   * Checks proxy headers in order: X-Forwarded-For, X-Real-IP, then RemoteAddr.
   *
   * @param request the HTTP servlet request
   * @return the client IP address
   */
  public static String extract(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}

