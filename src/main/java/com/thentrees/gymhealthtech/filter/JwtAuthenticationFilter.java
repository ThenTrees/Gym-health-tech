package com.thentrees.gymhealthtech.filter;

import com.thentrees.gymhealthtech.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Order(2)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("No JWT token found");
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = authHeader.substring(7);
    String userEmail = null;

    try {
      userEmail = jwtService.extractUsername(jwt);
    } catch (Exception e) {
      log.warn("Failed to extract username from JWT: {}", e.getMessage());
      filterChain.doFilter(request, response);
      return;
    }

    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if (userEmail != null && (existingAuth == null || existingAuth instanceof AnonymousAuthenticationToken)) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
      if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        String userId = jwtService.extractUserId(jwt).toString();
        if (userId != null) {
          MDC.put("userId", userId);
        }
        log.debug("Authenticated user: {}", userEmail);
      } else {
        log.warn("Invalid JWT token for user: {}", userEmail);
      }
    }

    filterChain.doFilter(request, response);
  }
}
