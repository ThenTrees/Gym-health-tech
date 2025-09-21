package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.*;
import com.thentrees.gymhealthtech.dto.response.AuthResponse;

public interface AuthenticationService {
  void verifyEmail(EmailVerificationRequest request);

  AuthResponse authenticate(LoginRequest request, String userAgent, String ipAddress);

  AuthResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress);

  void resendVerificationEmail(ResendVerificationRequest request);

  void logout(LogoutRequest request, String currentUserEmail);

  void changePassword(ChangePasswordRequest request, String currentUserEmail);
}
