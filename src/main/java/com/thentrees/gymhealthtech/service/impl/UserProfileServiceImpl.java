package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.common.UserStatus;
import com.thentrees.gymhealthtech.dto.request.UpdateProfileRequest;
import com.thentrees.gymhealthtech.dto.response.UserProfileResponse;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.model.UserProfile;
import com.thentrees.gymhealthtech.repository.UserProfileRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.UserProfileService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;

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

    if (request.getDob() != null) {
      profile.setDateOfBirth(request.getDob());
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
    if (profile.getHeightCm() != null && profile.getWeightKg() != null) {
      profile.setBmi(calculateBMI(profile.getHeightCm(), profile.getWeightKg()));
    }

    UserProfile savedProfile = userProfileRepository.save(profile);

    return mapToResponse(savedProfile);
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
        .dateOfBirth(profile.getDateOfBirth())
        .heightCm(profile.getHeightCm())
        .weightKg(profile.getWeightKg())
        .bmi(profile.getBmi())
        .healthNotes(profile.getHealthNotes())
        .timezone(profile.getTimezone())
        .unitWeight(profile.getUnitWeight())
        .unitLength(profile.getUnitLength())
        .createdAt(profile.getCreatedAt())
        .updatedAt(profile.getUpdatedAt())
        .build();
  }
}
