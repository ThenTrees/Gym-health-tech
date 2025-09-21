package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.ForgotPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.ResetPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.VerifyOtpRequest;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.RateLimitExceededException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.EmailService;
import com.thentrees.gymhealthtech.service.RedisService;
import com.thentrees.gymhealthtech.service.UserService;
import com.thentrees.gymhealthtech.util.RateLimitService;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final RateLimitService rateLimitService;
  private final RedisService redisService;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.verification.forgot-password.expiration:36000}")
  private int otpExpirySeconds;

  @Value("${app.otp.length:6}")
  private int otpLength;

  @Value("${app.reset-token.expiry-seconds:600}")
  private int resetTokenExpirySeconds;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public User getUserById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(String.format("User with id %s not found", id)));
  }

  @Override
  public User getUserByUsername(String username) {
    return userRepository
        .findByEmail(username)
        .orElseThrow(
            () ->
                new BusinessException(String.format("User with username %s not found", username)));
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest request) {
    String email = request.getEmail().toLowerCase().trim();

    // Rate limiting
    if (!rateLimitService.allowOtpRequest(email)) {
      throw new RateLimitExceededException(
          "Quá nhiều yêu cầu OTP. Vui lòng thử lại sau 15 phút.", 5);
    }

    // Check if user exists
    if (!userRepository.existsByEmail(email)) {
      throw new ResourceNotFoundException("Email không tồn tại trong hệ thống");
    }

    // Generate OTP
    String otp = generateOtp();

    // Store OTP in Redis
    String otpKey = redisService.getOtpKey(email);
    redisService.set(otpKey, otp, Duration.ofSeconds(otpExpirySeconds));

    // Send email
    try {
      emailService.sendOtpEmail(email, otp);
    } catch (Exception e) {
      redisService.delete(otpKey);
      log.error("Error sending OTP email to {}: {}", email, e.getMessage());
      throw e;
    }

    log.info("OTP generated and sent for email: {}", email);
  }

  @Override
  public String verifyOtp(VerifyOtpRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    String otp = request.getOtp().trim();

    // Rate limiting
    if (!rateLimitService.allowVerifyAttempt(email)) {
      return "Quá nhiều lần thử OTP sai. Vui lòng thử lại sau 15 phút.";
    }

    // Validation
    if (!isValidOtp(otp)) {
      return "OTP phải là " + otpLength + " chữ số";
    }

    // Get OTP from Redis
    String otpKey = redisService.getOtpKey(email);
    Object storedOtp = redisService.get(otpKey);

    if (storedOtp == null) {
      return "OTP đã hết hạn hoặc không tồn tại";
    }

    if (!storedOtp.toString().equals(otp)) {
      return "OTP không chính xác";
    }

    // OTP valid - delete it and create reset token
    redisService.delete(otpKey);

    log.info("OTP verified successfully for email: {}", email);
    return "OTP xác thực thành công";
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    String newPassword = request.getNewPassword();
    String confirmPassword = request.getConfirmPassword();

    // Validation
    if (!newPassword.equals(confirmPassword)) {
      throw new BusinessException("Mật khẩu xác nhận không khớp");
    }

    if (newPassword.length() < 8) {
      throw new BusinessException("Mật khẩu phải có ít nhất 8 ký tự");
    }

    // Find user and update password
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

    String hashedPassword = passwordEncoder.encode(newPassword);
    user.setPasswordHash(hashedPassword);
    userRepository.save(user);

    log.info("Password reset successfully for email: {}", email);
  }

  private String generateOtp() {
    int min = (int) Math.pow(10, otpLength - 1);
    int max = (int) Math.pow(10, otpLength) - 1;
    int otp = secureRandom.nextInt(max - min + 1) + min;
    return String.valueOf(otp);
  }

  private boolean isValidOtp(String otp) {
    return otp != null && otp.length() == otpLength && otp.matches("\\d+");
  }
}
