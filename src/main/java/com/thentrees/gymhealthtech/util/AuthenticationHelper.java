package com.thentrees.gymhealthtech.util;

import com.thentrees.gymhealthtech.exception.UnauthorizedException;
import com.thentrees.gymhealthtech.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for extracting user information from Spring Security Authentication.
 * Provides safe and consistent methods for accessing the current authenticated user.
 */
public final class AuthenticationHelper {

  private AuthenticationHelper() {
    // Utility class - prevent instantiation
  }

  /**
   * Extracts the User object from the Authentication principal.
   *
   * @param authentication the Spring Security authentication object
   * @return the User object
   * @throws UnauthorizedException if authentication is null or principal is not a User
   */
  public static User getCurrentUser(Authentication authentication) {
    if (authentication == null) {
      throw new UnauthorizedException("Authentication required");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof User)) {
      throw new UnauthorizedException("Invalid authentication principal");
    }

    return (User) principal;
  }

  /**
   * Gets the current authenticated user from SecurityContextHolder.
   *
   * @return the User object
   * @throws UnauthorizedException if no authentication is found in the security context
   */
  public static User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return getCurrentUser(authentication);
  }

  /**
   * Safely extracts the User object from Authentication, returns null if not available.
   *
   * @param authentication the Spring Security authentication object
   * @return the User object, or null if not available
   */
  public static User getCurrentUserOrNull(Authentication authentication) {
    if (authentication == null) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
      return (User) principal;
    }

    return null;
  }
}

