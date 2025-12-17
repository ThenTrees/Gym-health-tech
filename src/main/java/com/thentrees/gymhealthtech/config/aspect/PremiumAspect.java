package com.thentrees.gymhealthtech.config.aspect;

import com.thentrees.gymhealthtech.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "PREMIUM_ASPECT")
public class PremiumAspect {

  @Before("@annotation(com.thentrees.gymhealthtech.custom.PremiumOnly) || "
    + "@within(com.thentrees.gymhealthtech.custom.PremiumOnly)")
  public void checkPremiumAccess() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof User user)) {
      log.warn("User is not authenticated");
      throw new AccessDeniedException("Unauthorized");
    }

    LocalDateTime now = LocalDateTime.now();

    boolean validPremium =
      Boolean.TRUE.equals(user.getIsPremium())
        && user.getPremiumExpiresAt() != null
        && user.getPremiumExpiresAt().isAfter(now);

    if (!validPremium) {
      log.warn("Access denied. User {} is not premium or expired.", user.getId());
      throw new AccessDeniedException("Premium required");
    }

    log.debug("Premium access granted for user {}", user.getId());
  }
}
