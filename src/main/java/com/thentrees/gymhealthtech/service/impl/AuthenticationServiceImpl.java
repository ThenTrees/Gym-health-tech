package com.thentrees.gymhealthtech.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.AuthResponse;
import com.thentrees.gymhealthtech.enums.UserStatus;
import com.thentrees.gymhealthtech.enums.VerificationType;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.exception.UnauthorizedException;
import com.thentrees.gymhealthtech.model.RefreshToken;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.model.VerificationToken;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.repository.VerificationTokenRepository;
import com.thentrees.gymhealthtech.service.AuthenticationService;
import com.thentrees.gymhealthtech.service.JwtService;
import com.thentrees.gymhealthtech.service.RefreshTokenService;
import com.thentrees.gymhealthtech.service.VerificationTokenService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;

  @Value("${app.jwt.expiration}")
  private long jwtExpiration;

  // Web Client ID from Firebase Console (for Google Sign-In)
  private static final String GOOGLE_WEB_CLIENT_ID = "367621402821-jqemc9j1prn12u7q8qbq8oujk6dlhtmc.apps.googleusercontent.com";

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

    if (Boolean.TRUE.equals(user.getEmailVerified())) {
      throw new BusinessException("Email already verified");
    }

    // Check if there's an active token
    Optional<VerificationToken> existingToken =
      verificationTokenRepository.findActiveTokenByUserAndType(
        user.getId(), VerificationType.EMAIL, LocalDateTime.now());

    if (existingToken.isPresent()) {
      throw new BusinessException(
        "Verification email already sent. Please check your inbox or wait before requesting again.");
    }

    // Generate and send new verification token
    verificationTokenService.generateAndSendVerificationToken(user);
  }

  @Transactional
  @Override
  public void logout(LogoutRequest request, String currentUserEmail) {
    User user =
      userRepository
        .findByEmail(currentUserEmail)
        .orElseThrow(() -> new BusinessException("User not found"));

    if (Boolean.TRUE.equals(request.getLogoutFromAllDevices())) {
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

  @Override
  public AuthResponse loginWithFirebase(String idToken) {
    try {
      // Verify Google ID token using Google API Client Library
      // This accepts tokens from Google Sign-In (aud = Web Client ID)
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(),
        GsonFactory.getDefaultInstance()
      )
        .setAudience(Collections.singletonList(GOOGLE_WEB_CLIENT_ID))
        .build();

      GoogleIdToken googleIdToken = verifier.verify(idToken);
      if (googleIdToken == null) {
        log.error("Invalid Google ID token");
        throw new UnauthorizedException("Invalid Google ID token");
      }

      GoogleIdToken.Payload payload = googleIdToken.getPayload();
      String email = payload.getEmail();
      String name = (String) payload.get("name");
      String picture = (String) payload.get("picture");
      Boolean emailVerified = payload.getEmailVerified();

      if (email == null || email.isEmpty()) {
        log.error("Email not found in token");
        throw new UnauthorizedException("Email not found in token");
      }

      log.info("Google Sign-In successful for email: {}", email);

      // Tạo hoặc lấy user trong DB
      User user = userRepository.findByEmail(email)
        .orElseGet(() -> createUser(email, name, picture, emailVerified != null && emailVerified));

      // Tạo access token và refresh token của hệ thống riêng
      String accessToken = jwtService.generateTokenForUser(user);
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, null, null);

      List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
      UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.info("Google login successful for user: {}", user.getEmail());
      return buildAuthResponse(user, accessToken, refreshToken.getTokenHash());

    } catch (Exception e) {
      log.error("Google login failed: {}", e.getMessage(), e);
      throw new UnauthorizedException("Google Sign-In failed: " + e.getMessage());
    }
  }

  private User createUser(String email, String name, String picture, boolean emailVerify) {
    User user = new User();
    user.setEmail(email);
    user.setEmailVerified(emailVerify);
    UserProfile profile = createUserProfile(user, name, picture);
    user.setProfile(profile);
    user.setProfileCompleted(false);
    return userRepository.save(user);
  }

  private UserProfile createUserProfile(User user,String name, String picture){
    return UserProfile.builder()
      .user(user)
      .fullName(name)
      .avatarUrl(picture)
      .build();
  }

  private void validateUserAccount(User user) {
    switch (user.getStatus()) {
      case INACTIVE, DELETED -> throw new BusinessException("identifier or password is incorrect");
      case SUSPENDED -> throw new BusinessException("Account is suspended");
      case PENDING_VERIFICATION -> throw new BusinessException("Account is pending verification");
      case ACTIVE -> {
        // Active users are valid, no action needed
      }
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
