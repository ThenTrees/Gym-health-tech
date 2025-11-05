package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.RegisterRequest;
import com.thentrees.gymhealthtech.dto.response.RegisterResponse;

public interface UserRegistrationService {
  RegisterResponse registerUser(RegisterRequest request);
  boolean existsByEmail(String email);
}
