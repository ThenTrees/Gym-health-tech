package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.response.UserProfileResponse;
import com.thentrees.gymhealthtech.model.UserProfile;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
  UserProfileResponse getUserProfile();

  UserProfile getUserProfileById(UUID userId);

  void deleteProfile(UUID userId);

  void deleteProfile();

  UserProfileResponse updateUserProfile(UpdateProfileRequest request);

  String uploadProfileImage(MultipartFile file);
}
