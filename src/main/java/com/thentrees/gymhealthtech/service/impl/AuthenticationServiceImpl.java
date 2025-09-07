package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.common.VerificationType;
import com.thentrees.gymhealthtech.dto.request.ChangePasswordRequest;
import com.thentrees.gymhealthtech.dto.request.EmailVerificationRequest;
import com.thentrees.gymhealthtech.dto.request.LoginRequest;
import com.thentrees.gymhealthtech.dto.request.LogoutRequest;
import com.thentrees.gymhealthtech.dto.request.RefreshTokenRequest;
import com.thentrees.gymhealthtech.dto.request.ResendVerificationRequest;
import com.thentrees.gymhealthtech.dto.response.AuthResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
    log.info("Email verification attempt");

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
      throw new BusinessException("Verification token already used");
    }

    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessException("Verification token expired");
    }

    // Mark token as consumed
    verificationToken.setConsumedAt(LocalDateTime.now());
    verificationTokenRepository.save(verificationToken);

    // Mark user email as verified
    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    userRepository.save(user);

    log.info("Email verification successful for user: {}", user.getEmail());
  }

  @Transactional
  @Override
  public AuthResponse authenticate(LoginRequest request, String userAgent, String ipAddress) {
    log.info("Authentication attempt for identifier: {}", request.getIdentifier());
    try {
      // Authenticate user
//      Authentication authentication =
//          authenticationManager.authenticate(
//              new UsernamePasswordAuthenticationToken(
//                  request.getIdentifier(), request.getPassword()));

      // Get user details
      User user =
          userRepository
              .findByEmailOrPhone(request.getIdentifier())
              .orElseThrow(() -> new BusinessException("identifier or password is incorrect"));

      // Check if user account is active
      validateUserAccount(user);

      // ! BUGFIX: The accessToken and refreshToken values were hardcoded strings.
      String accessToken = jwtService.generateTokenForUser(user);
      RefreshToken refreshToken =
          refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

      log.info("Authentication successful for user: {}", user.getEmail());

      return buildAuthResponse(user, accessToken, refreshToken.getTokenHash());

    } catch (Exception e) {
      log.warn("Authentication failed for identifier: {}", request.getIdentifier());
      throw new BusinessException("identifier or password is incorrect");
    }
  }

  @Transactional
  @Override
  public AuthResponse refreshToken(
      RefreshTokenRequest request, String userAgent, String ipAddress) {
    log.info("Refresh token attempt");

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

    log.info("Token refresh successful for user: {}", user.getEmail());

    return buildAuthResponse(user, newAccessToken, newRefreshToken.getTokenHash());
  }

  @Transactional
  @Override
  public void resendVerificationEmail(ResendVerificationRequest request) {
    log.info("Resend verification email for: {}", request.getEmail());

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("User not found"));

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
    log.info("Logout attempt for user: {}", currentUserEmail);

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

  @Transactional
  @Override
  public void changePassword(ChangePasswordRequest request, String currentUserEmail) {
    log.info("Change password attempt for user: {}", currentUserEmail);

    User user =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

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

    log.info("Password changed successfully for user: {}", currentUserEmail);
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
