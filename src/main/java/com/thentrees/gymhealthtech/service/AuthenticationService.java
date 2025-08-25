package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.EmailVerificationRequest;
import com.thentrees.gymhealthtech.dto.request.LoginRequest;
import com.thentrees.gymhealthtech.dto.request.LogoutRequest;
import com.thentrees.gymhealthtech.dto.request.RefreshTokenRequest;
import com.thentrees.gymhealthtech.dto.response.AuthResponse;

public interface AuthenticationService {
  void verifyEmail(EmailVerificationRequest request);

  AuthResponse authenticate(LoginRequest request, String userAgent, String ipAddress);

  AuthResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress);

  void logout(LogoutRequest request, String currentUserEmail);
}
