package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Async("mailExecutor")
  @Retryable(
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2.0),
      include = {MailSendException.class, MessagingException.class})
  public void sendEmailVerification(String to, String fullName, String token) {
    try {
      String verificationUrl = frontendUrl + "/api/v1/auth/verify-email?token=" + token;

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

  @Async("mailExecutor")
  @Retryable(
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2.0),
      include = {MailSendException.class, MessagingException.class})
  public void sendOtpEmail(String toEmail, String otp) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject("Mã OTP đặt lại mật khẩu");

      String htmlContent = buildOtpEmailTemplate(otp);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("OTP email sent successfully to: {}", toEmail);

    } catch (MessagingException e) {
      log.error("Error sending OTP email to: {}", toEmail, e);
      throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.", e);
    }
  }

  private String buildVerificationEmailText(String fullName, String verificationUrl) {
    return String.format(
        """
            Hello %s,

            Welcome to Gym-Mate! Please verify your email address by clicking the link below:

            %s

            This link will expire in 24 hours.

            If you didn't create an account with us, please ignore this email.

            Best regards,
            Gym-Mate Team
            """,
        fullName, verificationUrl);
  }

  private String buildOtpEmailTemplate(String otp) {
    return String.format(
        """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #333; margin-bottom: 10px;">Đặt lại mật khẩu</h1>
                </div>

                <div style="background-color: #f8f9fa; padding: 25px; border-radius: 8px; margin-bottom: 25px;">
                    <p style="font-size: 16px; color: #333; margin-bottom: 20px;">
                        Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã OTP sau:
                    </p>

                    <div style="background-color: #007bff; color: white; padding: 15px; text-align: center;
                                font-size: 28px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>

                    <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin-top: 20px;">
                        <p style="color: #856404; margin: 0; font-weight: bold;">
                            ⏰ Mã OTP này sẽ hết hạn sau 30 phút
                        </p>
                    </div>
                </div>

                <div style="border-top: 1px solid #dee2e6; padding-top: 20px;">
                    <p style="color: #6c757d; font-size: 14px; margin: 0;">
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                    </p>
                </div>
            </div>
            """,
        otp);
  }
}
