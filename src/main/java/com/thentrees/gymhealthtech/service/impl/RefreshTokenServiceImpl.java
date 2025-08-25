package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.model.RefreshToken;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.RefreshTokenRepository;
import com.thentrees.gymhealthtech.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${app.jwt.refresh-expiration:604800000}") // 7 days
  private long refreshTokenExpiration;

  @Transactional
  @Override
  public RefreshToken createRefreshToken(User user, String userAgent, String ipAddress) {
    try {
      // Revoke existing refresh tokens for this user
      refreshTokenRepository.revokeAllByUserId(user.getId());

      // Generate new refresh token
      byte[] tokenBytes = new byte[64];
      secureRandom.nextBytes(tokenBytes);
      String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

      String tokenHash = hashToken(token);

      RefreshToken refreshToken = new RefreshToken();
      refreshToken.setUser(user);
      refreshToken.setTokenHash(tokenHash);
      refreshToken.setExpiresAt(OffsetDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
      refreshToken.setUserAgent(userAgent);
      refreshToken.setIp(ipAddress);

      RefreshToken saved = refreshTokenRepository.save(refreshToken);
      log.info("Created refresh token for user: {}", user.getEmail());

      // Store the plain token temporarily for returning to client
      saved.setTokenHash(token); // This is a bit hacky but works for this use case
      return saved;
    } catch (Exception e) {
      log.error("Error creating refresh token for user: {}", user.getEmail(), e);
      throw e;
    }
  }

  @Transactional
  @Override
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findActiveTokens()
      .stream()
      .filter(rt -> passwordEncoder.matches(token, rt.getTokenHash()))
      .findFirst();
  }

  @Transactional
  @Override
  public void revokeToken(String token) {
    Optional<RefreshToken> refreshToken = findByToken(token);
    refreshToken.ifPresent(rt -> {
      rt.setRevokedAt(OffsetDateTime.now());
      refreshTokenRepository.save(rt);
      log.info("Revoked refresh token for user: {}", rt.getUser().getEmail());
    });
  }

  @Transactional
  @Override
  public void revokeAllUserTokens(User user) {
    refreshTokenRepository.revokeAllByUserId(user.getId());
    log.info("Revoked all refresh tokens for user: {}", user.getEmail());
  }

  @Override
  public boolean isTokenExpired(RefreshToken token) {
    return token.getExpiresAt().isBefore(OffsetDateTime.now());
  }

  @Override
  public boolean isTokenRevoked(RefreshToken token) {
    return token.getRevokedAt() != null;
  }
  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException("Error hashing refresh token", e);
    }
  }
}
