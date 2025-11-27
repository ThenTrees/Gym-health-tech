package com.thentrees.gymhealthtech.filter;

import com.thentrees.gymhealthtech.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SignatureException;

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
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("No JWT token found");
      filterChain.doFilter(request, response);
      return;
    }

    final String rawToken = authHeader.substring(7);

    // 1. Validate token
    String userEmail;
    try {
      userEmail = jwtService.extractUsername(rawToken);

    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT for request {} {}", request.getMethod(), request.getRequestURI());
      writeUnauthorized(response, "JWT expired", "JWT_EXPIRED");
      return;

    } catch (MalformedJwtException e) {
      log.warn("Invalid JWT signature for request {} {}", request.getMethod(), request.getRequestURI());
      writeUnauthorized(response, "Invalid JWT token", "JWT_INVALID");
      return;

    } catch (JwtException e) {
      log.warn("Failed JWT parsing for request {} {}", request.getMethod(), request.getRequestURI());
      writeUnauthorized(response, "Invalid JWT token", "JWT_INVALID");
      return;
    }

    // 2. Kiểm tra authentication hiện tại
    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if (userEmail != null && (existingAuth == null || existingAuth instanceof AnonymousAuthenticationToken)) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

      if (jwtService.isTokenValid(rawToken, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Put userId vào MDC
        String userId = jwtService.extractUserId(rawToken).toString();
        MDC.put("userId", userId);

        log.debug("Authenticated user: {}", userEmail);

      } else {
        log.warn("JWT token failed validation for user {}", userEmail);
        writeUnauthorized(response, "Invalid JWT token", "JWT_INVALID");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }


  private void writeUnauthorized(HttpServletResponse response, String message, String code) throws IOException {
    if (response.isCommitted()) return;

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    response.getWriter().write("""
                {
                    "message": "%s",
                    "code": "%s",
                    "status": 401
                }
                """.formatted(message, code));

    // cleanup
    SecurityContextHolder.clearContext();
    MDC.remove("userId");
  }
}
