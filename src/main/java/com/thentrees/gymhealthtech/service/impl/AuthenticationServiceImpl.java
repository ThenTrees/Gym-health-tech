package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.common.VerificationType;
import com.thentrees.gymhealthtech.dto.request.EmailVerificationRequest;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.VerificationToken;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.repository.VerificationTokenRepository;
import com.thentrees.gymhealthtech.service.AuthenticationService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final VerificationTokenRepository verificationTokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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

    if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new BusinessException("Verification token expired");
    }

    // Mark token as consumed
    verificationToken.setConsumedAt(OffsetDateTime.now());
    verificationTokenRepository.save(verificationToken);

    // Mark user email as verified
    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    userRepository.save(user);

    log.info("Email verification successful for user: {}", user.getEmail());
  }
}
