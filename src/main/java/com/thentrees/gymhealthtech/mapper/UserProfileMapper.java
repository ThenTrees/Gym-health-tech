package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.UserProfileResponse;
import com.thentrees.gymhealthtech.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  @Mapping(target = "userId", expression = "java(profile.getUser() != null ? profile.getUser().getId() : null)")
  @Mapping(target = "profileImageUrl", source = "avatarUrl")
  @Mapping(target = "bmiCategory", ignore = true)
  UserProfileResponse toResponse(UserProfile profile);
}

