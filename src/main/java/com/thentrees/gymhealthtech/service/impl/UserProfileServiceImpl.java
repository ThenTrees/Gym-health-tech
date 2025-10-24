package com.thentrees.gymhealthtech.service.impl;

import static com.thentrees.gymhealthtech.constant.S3Constant.*;

import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.response.UserProfileResponse;
import com.thentrees.gymhealthtech.enums.UserStatus;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.repository.UserProfileRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.UserProfileService;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final S3Util s3Util;
  private final FileValidator fileValidator;

  @Override
  public UserProfileResponse getUserProfile(String email) {
    log.info("Fetching user profile for email or phone: {}", email);
    User userExist =
        userRepository
            .findByEmailOrPhone(email)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("User not found with email or phone: " + email));

    UserProfile profile =
        userProfileRepository
            .findByUserId(userExist.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    return mapToResponse(profile);
  }

  @Override
  public UserProfile getUserProfileById(UUID userId) {
    return userProfileRepository
        .findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
  }

  @Override
  @Transactional
  public void deleteProfile(UUID userId) {

    User userExist =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new BusinessException("User not found with email or phone: " + userId));

    UserProfile userProfile =
        userProfileRepository
            .findByUserId(userExist.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    userExist.setStatus(UserStatus.DELETED);
    userProfile.setIsDeleted(true);
    userExist.setIsDeleted(true);

    userRepository.save(userExist);
    userProfileRepository.save(userProfile);
  }

  @Override
  public void deleteProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info("Delete user profile: {}", authentication.getName());
    User userExist =
        userRepository
            .findByEmailOrPhone(authentication.getName())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "User not found with email or phone: " + authentication.getName()));

    UserProfile userProfile =
        userProfileRepository
            .findByUserId(userExist.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    userExist.setStatus(UserStatus.DELETED);
    userProfile.setIsDeleted(true);
    userExist.setIsDeleted(true);

    userRepository.save(userExist);
    userProfileRepository.save(userProfile);
  }

  @Override
  @Transactional
  public UserProfileResponse updateUserProfile(UpdateProfileRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    log.info("Update user profile: {}", authentication.getName());
    User userExist =
        userRepository
            .findByEmailOrPhone(authentication.getName())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "User not found with email or phone: " + authentication.getName()));

    UserProfile profile =
        userProfileRepository
            .findByUserId(userExist.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

    // Update fields
    if (request.getFullName() != null) {
      profile.setFullName(request.getFullName());
    }

    if (request.getGender() != null) {
      profile.setGender(request.getGender());
    }

    if (request.getAge() != null) {
      profile.setAge(request.getAge());
    }

    if (request.getHeightCm() != null) {
      profile.setHeightCm(request.getHeightCm());
    }

    if (request.getWeightKg() != null) {
      profile.setWeightKg(request.getWeightKg());
    }

    if (request.getHealthNotes() != null) {
      profile.setHealthNotes(request.getHealthNotes());
    }

    if (request.getUnitWeight() != null) {
      profile.setUnitWeight(request.getUnitWeight());
    }

    if (request.getUnitLength() != null) {
      profile.setUnitLength(request.getUnitLength());
    }

    // Recalculate BMI if height or weight changed
    if (request.getHeightCm() != null && request.getWeightKg() != null) {
      profile.setBmi(calculateBMI(request.getHeightCm(), request.getWeightKg()));
    }

    if (request.getFitnessLevel() != null) {
      profile.setFitnessLevel(request.getFitnessLevel());
    }

    UserProfile savedProfile = userProfileRepository.save(profile);

    return mapToResponse(savedProfile);
  }

  @Transactional
  @Override
  public String uploadProfileImage(MultipartFile file) {
    fileValidator.validateImage(file);
    String fileUrl = null;
    try {
      fileUrl = s3Util.uploadFile(file, S3_AVATAR_FOLDER);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User userExist =
          userRepository
              .findByEmailOrPhone(authentication.getName())
              .orElseThrow(
                  () ->
                      new BusinessException(
                          "User not found with email or phone: " + authentication.getName()));

      UserProfile profile =
          userProfileRepository
              .findByUserId(userExist.getId())
              .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

      profile.setAvatarUrl(fileUrl);
      userProfileRepository.save(profile);

      return fileUrl;
    } catch (Exception e) {
      log.error("Error uploading profile image", e);
      if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
      throw new BusinessException("Failed to upload profile image", e.getMessage());
    }
  }

  private BigDecimal calculateBMI(BigDecimal heightCm, BigDecimal weightKg) {
    BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    return weightKg.divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
  }

  private UserProfileResponse mapToResponse(UserProfile profile) {
    return UserProfileResponse.builder()
        .userId(profile.getUser().getId())
        .fullName(profile.getFullName())
        .gender(profile.getGender())
        .age(profile.getAge())
        .heightCm(profile.getHeightCm())
        .weightKg(profile.getWeightKg())
        .bmi(profile.getBmi())
        .healthNotes(profile.getHealthNotes())
        .timezone(profile.getTimezone())
        .unitWeight(profile.getUnitWeight())
        .unitLength(profile.getUnitLength())
        .createdAt(profile.getCreatedAt())
        .updatedAt(profile.getUpdatedAt())
        .profileImageUrl(profile.getAvatarUrl())
        .fitnessLevel(profile.getFitnessLevel())
        .build();
  }
}
