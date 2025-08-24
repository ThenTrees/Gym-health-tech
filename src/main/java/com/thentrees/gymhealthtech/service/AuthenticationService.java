package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.EmailVerificationRequest;

public interface AuthenticationService {
  void verifyEmail(EmailVerificationRequest request);
}
