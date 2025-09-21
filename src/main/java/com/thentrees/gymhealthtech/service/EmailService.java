package com.thentrees.gymhealthtech.service;

public interface EmailService {
  void sendEmailVerification(String to, String fullName, String token);

  void sendOtpEmail(String to, String otp);
}
