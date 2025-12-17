package com.thentrees.gymhealthtech.filter;

import com.thentrees.gymhealthtech.util.TraceIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class MDCFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {

    String traceId = TraceIdGenerator.generate();
    MDC.put("traceId", traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("traceId");
    }
  }
}
