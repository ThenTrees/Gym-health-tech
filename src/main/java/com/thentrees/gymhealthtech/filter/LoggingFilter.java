package com.thentrees.gymhealthtech.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Component
@Order(3) // Sau Jwt filter, trước DispatcherServlet
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    HttpServletRequest httpReq = (HttpServletRequest) request;
    HttpServletResponse httpRes = (HttpServletResponse) response;

    long start = System.currentTimeMillis();

    log.info("Incoming request: method={}, uri={}",
      httpReq.getMethod(), httpReq.getRequestURI());

    chain.doFilter(request, response);

    long duration = System.currentTimeMillis() - start;

    log.info("Response: status={}, duration={}ms",
      httpRes.getStatus(), duration);
  }
}
