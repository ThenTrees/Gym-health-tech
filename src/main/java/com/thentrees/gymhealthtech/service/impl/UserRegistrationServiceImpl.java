package com.thentrees.gymhealthtech.service.impl;

import static com.thentrees.gymhealthtech.constant.ValidationMessages.*;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.constant.ErrorMessages;
import com.thentrees.gymhealthtech.constant.SuccessMessages;
import com.thentrees.gymhealthtech.dto.request.RegisterRequest;
import com.thentrees.gymhealthtech.dto.response.RegisterResponse;
import com.thentrees.gymhealthtech.enums.UserRole;
import com.thentrees.gymhealthtech.enums.UserStatus;
import com.thentrees.gymhealthtech.enums.VerificationType;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.model.VerificationToken;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.repository.VerificationTokenRepository;
import com.thentrees.gymhealthtech.service.EmailService;
import com.thentrees.gymhealthtech.service.UserRegistrationService;
import com.thentrees.gymhealthtech.util.ResourceAlreadyExists;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER_REGISTER-SERVICE")
public class UserRegistrationServiceImpl implements UserRegistrationService {
  private final UserRepository userRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  @Override
  public RegisterResponse registerUser(RegisterRequest request) {
    if(!validatePasswordsMatch(request.getPassword(), request.getConfirmPassword())){
      log.error(PASSWORDS_DO_NOT_MATCH);
      throw new BusinessException(PASSWORDS_DO_NOT_MATCH);
    }

    if(validateUserExists(request.getEmail(), request.getPhone())){
      log.error(ErrorMessages.EMAIL_OR_PASSWORD_ALREADY_EXIST);
      throw new BusinessException(ResourceAlreadyExists.formatMessage("User", "email", request.getEmail()));
    }

    User user = createUser(request);

    UserProfile profile = createUserProfile(user, request);
    user.setProfile(profile);

    user = userRepository.save(user);

    // Generate and send verification token
    generateAndSendVerificationToken(user);
    return buildRegisterResponse(user);
  }

  private boolean validatePasswordsMatch(String password, String confirmPassword) {
    return password.equals(confirmPassword);
  }

  private boolean validateUserExists(String email, String phone) {
    return existsByEmail(email) || existsByPhone(phone);
  }

  private User createUser(RegisterRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setStatus(UserStatus.PENDING_VERIFICATION);
    user.setRole(UserRole.USER);
    user.setEmailVerified(false);

    return user;
  }

  private UserProfile createUserProfile(User user, RegisterRequest request) {
    UserProfile profile = new UserProfile();
    profile.setUser(user);
    profile.setFullName(request.getFullName());
    profile.setGender(request.getGender());
    profile.setAge(request.getAge());
    profile.setHeightCm(request.getHeightCm());
    profile.setWeightKg(request.getWeightKg());
    profile.setHealthNotes(request.getHealthNotes());
    profile.setUnitWeight(request.getUnitWeight());
    profile.setUnitLength(request.getUnitLength());
    profile.setFitnessLevel(request.getFitnessLevel());

    // Calculate BMI if height and weight are provided
    if (request.getHeightCm() != null && request.getWeightKg() != null) {
      BigDecimal bmi = calculateBMI(request.getWeightKg(), request.getHeightCm());
      profile.setBmi(bmi);
    }

    return profile;
  }

  private BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
    // BMI = weight (kg) / (height (m))^2
    BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    BigDecimal heightSquared = heightM.multiply(heightM);
    return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
  }

  private void generateAndSendVerificationToken(User user) {
    // Generate secure token
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    String tokenHash = passwordEncoder.encode(token);

    // Create verification token
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setUser(user);
    verificationToken.setType(VerificationType.EMAIL);
    verificationToken.setTokenHash(tokenHash);
    verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours expiry

    verificationTokenRepository.save(verificationToken);
    try {
      emailService.sendEmailVerification(user.getEmail(), user.getProfile().getFullName(), token);
    } catch (Exception e) {
      log.error(ErrorMessages.SEND_MAIL_VERIFICATION_FAILED + user.getEmail());
    }
  }

  private RegisterResponse buildRegisterResponse(User user) {
    return RegisterResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .phone(user.getPhone())
        .fullName(user.getProfile().getFullName())
        .emailVerified(user.getEmailVerified())
        .createdAt(user.getCreatedAt())
        .message(SuccessMessages.USER_CREATED)
        .build();
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsByPhone(String phone) {
    return userRepository.existsByPhone(phone);
  }
}
