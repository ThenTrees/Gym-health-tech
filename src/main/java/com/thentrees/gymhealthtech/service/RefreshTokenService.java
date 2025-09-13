package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.model.RefreshToken;
import com.thentrees.gymhealthtech.model.User;
import java.util.Optional;

public interface RefreshTokenService {
  RefreshToken createRefreshToken(User user, String userAgent, String ipAddress);

  Optional<RefreshToken> findByToken(String token);

  void revokeToken(String token);

  void revokeAllUserTokens(User user);

  boolean isTokenExpired(RefreshToken token);

  boolean isTokenRevoked(RefreshToken token);
}
