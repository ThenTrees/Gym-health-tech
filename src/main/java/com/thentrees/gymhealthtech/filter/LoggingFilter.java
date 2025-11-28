package com.thentrees.gymhealthtech.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(3)
public class LoggingFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
  private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // 1 giây

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    long startTime = System.currentTimeMillis();

    String uri = request.getRequestURI();
    try {

      // Bỏ qua log cho prometheus scrape
      if (uri.startsWith("/actuator/prometheus")) {
        filterChain.doFilter(request, response);
        return;
      }
      logger.info("Incoming request: method={}, uri={}", request.getMethod(), request.getRequestURI());
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      String traceId = MDC.get("traceId");
      String userId = MDC.get("userId");

      if (duration > SLOW_REQUEST_THRESHOLD_MS) {
        logger.warn("traceId={} userId={} Slow request detected: method={} uri={} duration={}ms",
          traceId, userId,
          request.getMethod(), request.getRequestURI(), duration);
      } else {
        if (!uri.startsWith("/actuator/prometheus")) {
          logger.info("Request completed: method={} uri={} duration={}ms",
            request.getMethod(), request.getRequestURI(), duration);
        }
      }
    }
  }
}
