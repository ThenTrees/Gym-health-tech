package com.thentrees.gymhealthtech.filter;

import com.thentrees.gymhealthtech.util.GenerateTraceId;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
  private final GenerateTraceId generateTraceId;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpReq = (HttpServletRequest) request;
    HttpServletResponse httpRes = (HttpServletResponse) response;

    long startTime = System.currentTimeMillis();
    String requestId = UUID.randomUUID().toString();
    MDC.put("requestId", requestId); // generateTraceId

    try {
      log.info("Incoming request: method={}, uri={}", httpReq.getMethod(), httpReq.getRequestURI());

      chain.doFilter(request, response);

    } finally {
      long duration = System.currentTimeMillis() - startTime;
      log.info("Response: status={}, duration={}ms", httpRes.getStatus(), duration);
      MDC.clear();
    }
  }
}
