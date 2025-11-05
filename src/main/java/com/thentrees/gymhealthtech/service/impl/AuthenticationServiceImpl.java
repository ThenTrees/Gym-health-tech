package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.AuthResponse;
import com.thentrees.gymhealthtech.enums.UserStatus;
import com.thentrees.gymhealthtech.enums.VerificationType;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.RefreshToken;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.VerificationToken;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.repository.VerificationTokenRepository;
import com.thentrees.gymhealthtech.service.AuthenticationService;
import com.thentrees.gymhealthtech.service.JwtService;
import com.thentrees.gymhealthtech.service.RefreshTokenService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-SERVICE")
public class AuthenticationServiceImpl implements AuthenticationService {

  private final VerificationTokenRepository verificationTokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final EmailServiceImpl emailService;
  private final RefreshTokenService refreshTokenService;

  @Value("${app.jwt.expiration}")
  private long jwtExpiration;

  @Transactional
  @Override
  public void verifyEmail(EmailVerificationRequest request) {
    VerificationToken verificationToken =
        verificationTokenRepository
            .findByTokenHashAndType(request.getToken(), VerificationType.EMAIL)
            .orElse(null);

    // Check if token exists by comparing hashed versions
    if (verificationToken == null) {
      verificationToken =
          verificationTokenRepository.findAll().stream()
              .filter(token -> token.getType() == VerificationType.EMAIL)
              .filter(token -> passwordEncoder.matches(request.getToken(), token.getTokenHash()))
              .findFirst()
              .orElseThrow(() -> new BusinessException("Invalid verification token"));
    }

    if (verificationToken.getConsumedAt() != null) {
      log.error("Token has already been consumed");
      throw new BusinessException("Verification token already used");
    }

    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      log.error("Token has expired");
      throw new BusinessException("Verification token expired");
    }

    // Mark token as consumed
    verificationToken.setConsumedAt(LocalDateTime.now());
    verificationTokenRepository.save(verificationToken);

    // Mark user email as verified
    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
  }

  @Transactional
  @Override
  public AuthResponse authenticate(LoginRequest request, String userAgent, String ipAddress) {
    try {
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
      );

      User user = (User) authentication.getPrincipal();

      validateUserAccount(user);

      String accessToken = jwtService.generateTokenForUser(user);
      RefreshToken refreshToken =
        refreshTokenService.createRefreshToken(user, userAgent, ipAddress);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return buildAuthResponse(user, accessToken, refreshToken.getTokenHash());
    } catch (BadCredentialsException e) {
        throw new BusinessException("identifier or password is incorrect");
    } catch (DisabledException e) {
      throw new BusinessException("Account is disabled");
    } catch (LockedException e) {
      throw new BusinessException("Account is locked");
    }
  }

  @Transactional
  @Override
  public AuthResponse refreshToken(
      RefreshTokenRequest request, String userAgent, String ipAddress) {
    Optional<RefreshToken> refreshTokenOpt =
        refreshTokenService.findByToken(request.getRefreshToken());

    if (refreshTokenOpt.isEmpty()) {
      throw new BusinessException("Invalid refresh token");
    }

    RefreshToken refreshToken = refreshTokenOpt.get();

    if (refreshTokenService.isTokenExpired(refreshToken)) {
      throw new BusinessException("Refresh token expired");
    }

    if (refreshTokenService.isTokenRevoked(refreshToken)) {
      throw new BusinessException("Refresh token revoked");
    }

    User user = refreshToken.getUser();
    validateUserAccount(user);

    // Generate new access token
    String newAccessToken = jwtService.generateTokenForUser(user);

    // Generate new refresh token (rotate refresh tokens for security)
    RefreshToken newRefreshToken =
        refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

    // Revoke old refresh token
    refreshTokenService.revokeToken(request.getRefreshToken());
    return buildAuthResponse(user, newAccessToken, newRefreshToken.getTokenHash());
  }

  @Override
  public void resendVerificationEmail(ResendVerificationRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.getEmail()));

    if (user.getEmailVerified()) {
      throw new BusinessException("Email already verified");
    }

    // Check if there's an active token
    Optional<VerificationToken> existingToken =
        verificationTokenRepository.findActiveTokenByUserAndType(
            user.getId(), VerificationType.EMAIL, OffsetDateTime.now());

    if (existingToken.isPresent()) {
      throw new BusinessException(
          "Verification email already sent. Please check your inbox or wait before requesting again.");
    }

    // Generate and send new verification token
    generateAndSendVerificationToken(user);
  }

  @Transactional
  @Override
  public void logout(LogoutRequest request, String currentUserEmail) {
    User user =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

    if (request.getLogoutFromAllDevices()) {
      refreshTokenService.revokeAllUserTokens(user);
      log.info("Logged out from all devices for user: {}", currentUserEmail);
    } else if (request.getRefreshToken() != null) {
      refreshTokenService.revokeToken(request.getRefreshToken());
      log.info("Logged out from current device for user: {}", currentUserEmail);
    }
  }

  @Override
  public void changePassword(ChangePasswordRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    // Verify current password
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
      throw new BusinessException("Current password is incorrect");
    }

    // Validate new passwords match
    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      throw new BusinessException("New passwords do not match");
    }

    // Validate new password is different from current
    if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
      throw new BusinessException("New password must be different from current password");
    }

    // Update password
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Revoke all refresh tokens to force re-login on all devices
    refreshTokenService.revokeAllUserTokens(user);

    log.info("Password changed successfully for user: {}", user.getUsername());
  }

  private void validateUserAccount(User user) {
    switch (user.getStatus()) {
      case INACTIVE, DELETED -> throw new BusinessException("identifier or password is incorrect");
      case SUSPENDED -> throw new BusinessException("Account is suspended");
      case PENDING_VERIFICATION -> throw new BusinessException("Account is pending verification");
    }
  }

  /**
   * Generate and send email verification token
   *
   * @param user The user to send the verification email to
   */
  private void generateAndSendVerificationToken(User user) {
    // Generate secure token
    java.security.SecureRandom secureRandom = new java.security.SecureRandom();
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    String tokenHash = passwordEncoder.encode(token);

    // Delete existing verification tokens for this user
    verificationTokenRepository.deleteByUserIdAndType(user.getId(), VerificationType.EMAIL);

    // Create verification token
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setUser(user);
    verificationToken.setType(VerificationType.EMAIL);
    verificationToken.setTokenHash(tokenHash);
    verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours expiry

    verificationTokenRepository.save(verificationToken);

    // Send verification email (async)
    try {
      emailService.sendEmailVerification(user.getEmail(), user.getProfile().getFullName(), token);
    } catch (Exception e) {
      log.error("Failed to send verification email to: {}", user.getEmail(), e);
      // Don't fail the operation if email fails
    }
  }

  private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtExpiration / 1000) // Convert to seconds
        .user(
            AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getProfile() != null ? user.getProfile().getFullName() : null)
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build())
        .build();
  }
}
