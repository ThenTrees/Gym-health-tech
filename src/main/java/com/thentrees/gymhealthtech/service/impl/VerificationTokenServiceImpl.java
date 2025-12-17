package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.enums.VerificationType;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.VerificationToken;
import com.thentrees.gymhealthtech.repository.VerificationTokenRepository;
import com.thentrees.gymhealthtech.service.EmailService;
import com.thentrees.gymhealthtech.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service implementation for generating and sending verification tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "VERIFICATION-TOKEN-SERVICE")
public class VerificationTokenServiceImpl implements VerificationTokenService {

  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  @Override
  public void generateAndSendVerificationToken(User user) {
    // Generate secure token
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    String tokenHash = passwordEncoder.encode(token);

    // Delete existing verification tokens for this user to prevent multiple active tokens
    verificationTokenRepository.deleteByUserIdAndType(user.getId(), VerificationType.EMAIL);

    // Create verification token
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setUser(user);
    verificationToken.setType(VerificationType.EMAIL);
    verificationToken.setTokenHash(tokenHash);
    verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours expiry

    verificationTokenRepository.save(verificationToken);

    // Send verification email
    try {
      String fullName = user.getProfile() != null ? user.getProfile().getFullName() : "User";
      emailService.sendEmailVerification(user.getEmail(), fullName, token);
      log.info("Verification token generated and email sent to: {}", user.getEmail());
    } catch (Exception e) {
      log.error("Failed to send verification email to: {}", user.getEmail(), e);
      // Don't fail the operation if email fails - token is already saved
    }
  }
}

