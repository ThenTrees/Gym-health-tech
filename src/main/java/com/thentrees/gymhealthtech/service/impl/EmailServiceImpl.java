package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Async
  public void sendEmailVerification(String to, String fullName, String token) {
    try {
      String verificationUrl = frontendUrl + "/verify-email?token=" + token;

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject("Verify Your GymHealthTech Account");
      message.setText(buildVerificationEmailText(fullName, verificationUrl));

      mailSender.send(message);
      log.info("Verification email sent successfully to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send verification email to: {}", to, e);
      throw e;
    }
  }

  private String buildVerificationEmailText(String fullName, String verificationUrl) {
    return String.format(
        """
            Hello %s,

            Welcome to GymHealthTech! Please verify your email address by clicking the link below:

            %s

            This link will expire in 24 hours.

            If you didn't create an account with us, please ignore this email.

            Best regards,
            GymHealthTech Team
            """,
        fullName, verificationUrl);
  }
}
