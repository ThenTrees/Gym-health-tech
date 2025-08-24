package com.thentrees.gymhealthtech.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class GetRequestDetails {
  public String getRequestDetails(HttpServletRequest request) {
    return String.format("%s %s", request.getMethod(), request.getRequestURI());
  }

}
