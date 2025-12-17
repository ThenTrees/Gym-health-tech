package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.ForgotPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.ResetPasswordRequest;
import com.thentrees.gymhealthtech.dto.request.VerifyOtpRequest;
import com.thentrees.gymhealthtech.dto.response.PagedResponse;
import com.thentrees.gymhealthtech.dto.response.UserResponse;
import com.thentrees.gymhealthtech.model.User;
import java.util.UUID;

public interface UserService {
  User getUserById(UUID id);

  User getUserByUsername(String username);

  void forgotPassword(ForgotPasswordRequest email);

  String verifyOtp(VerifyOtpRequest request);

  void resetPassword(ResetPasswordRequest request);

  PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDirection);
}
