package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.RegisterRequest;
import com.thentrees.gymhealthtech.dto.response.RegisterResponse;
import org.springframework.transaction.annotation.Transactional;

public interface UserRegistrationService {
  @Transactional
  RegisterResponse registerUser(RegisterRequest request);

  boolean existsByEmail(String email);
}
