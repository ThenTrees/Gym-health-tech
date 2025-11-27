package com.thentrees.gymhealthtech.filter;
import com.thentrees.gymhealthtech.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimiterService rateLimiterService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String userId = MDC.get("userId");
    if (userId == null) userId = "anonymous";

    boolean allowed = rateLimiterService.tryConsume(userId);
    if (!allowed) {
      log.warn("Rate limit exceeded for userId={}", userId);
      response.setStatus(429);
      response.getWriter().write("Rate limit exceeded. Vui lòng thử lại sau.");
      return;
    }

    filterChain.doFilter(request, response);
  }
}

