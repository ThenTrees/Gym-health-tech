package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.dto.response.UserSummaryDTO;
import com.thentrees.gymhealthtech.model.*;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
  // ======= 2️⃣  Post → PostResponseDTO =======
  @Mapping(target = "user", expression = "java(toUserSummary(post.getUser()))")
  @Mapping(target = "planId", source = "plan.id")
  @Mapping(target = "comments", expression = "java(toCommentList(post.getComments()))")
  @Mapping(target = "postId", source = "id")
  PostResponse toResponse(Post post);

  default UserSummaryDTO toUserSummary(User user) {
    if (user == null) return null;
    String avatarUrl = null;
    try {
      avatarUrl = user.getProfile() != null ? user.getProfile().getAvatarUrl() : null;
    } catch (org.hibernate.LazyInitializationException e) {
      avatarUrl = null; // tránh crash nếu lazy load fail
    }

    return UserSummaryDTO.builder()
        .id(user.getId().toString())
        .username(user.getProfile().getFullName())
        .avatarUrl(avatarUrl)
        .build();
  }

  default List<PostCommentResponse> toCommentList(List<PostComment> comments) {
    if (comments == null) return List.of();
    return comments.stream()
        .map(
            c ->
                PostCommentResponse.builder()
                    .id(c.getId().toString())
                    .content(c.getContent())
                    .user(toUserSummary(c.getUser()))
                    .likesCount(c.getLikesCount())
                    .repliesCount(c.getRepliesCount())
                    .isPinned(c.getIsPinned())
                    .isActive(c.getIsActive())
                    .mediaUrl(c.getMediaUrl())
                    .createdAt(c.getCreatedAt())
                    .postId(c.getPost() != null ? c.getPost().getId().toString() : null)
                    .replies(toCommentList(c.getReplies())) // đệ quy an toàn
                    .build())
        .collect(Collectors.toList());
  }
}
