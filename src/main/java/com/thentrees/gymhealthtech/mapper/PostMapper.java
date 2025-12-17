package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.PlanSummaryResponse;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.dto.response.UserSummaryResponse;
import com.thentrees.gymhealthtech.model.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
  // ======= 2️⃣  Post → PostResponseDTO =======
  @Mapping(target = "user", expression = "java(toUserSummary(post.getUser()))")
  @Mapping(target = "planId", source = "plan.id")
  @Mapping(target = "plan", expression = "java(toPlanSummary(post.getPlan()))")
  @Mapping(target = "comments", expression = "java(toCommentList(post.getComments()))")
  @Mapping(target = "postId", source = "id")
  PostResponse toResponse(Post post);

  default UserSummaryResponse toUserSummary(User user) {
    if (user == null) return null;
    String avatarUrl = null;
    try {
      avatarUrl = user.getProfile() != null ? user.getProfile().getAvatarUrl() : null;
    } catch (org.hibernate.LazyInitializationException e) {
      avatarUrl = null; // tránh crash nếu lazy load fail
    }

    return UserSummaryResponse.builder()
        .id(user.getId().toString())
        .username(user.getProfile().getFullName())
        .avatarUrl(avatarUrl)
        .build();
  }

  default PlanSummaryResponse toPlanSummary(Plan plan) {
    if (plan == null) return null;

    PlanSummaryResponse dto = new PlanSummaryResponse();
    dto.setId(plan.getId().toString());
    dto.setTitle(plan.getTitle());
    dto.setDescription(plan.getDescription());
    dto.setSource(plan.getSource());
    dto.setStatus(plan.getStatus());
    dto.setCycleWeeks(plan.getCycleWeeks());

    // Calculate total days and exercises
    if (plan.getPlanDays() != null) {
      dto.setTotalDays(plan.getPlanDays().size());
      int totalExercises =
          plan.getPlanDays().stream()
            .filter(Objects::nonNull)
              .mapToInt(day -> day.getPlanItems() != null ? day.getPlanItems().size() : 0)
              .sum();
      dto.setTotalExercises(totalExercises);
    }

    // Get goal objective name
    if (plan.getGoal() != null && plan.getGoal().getObjective() != null) {
      dto.setGoalName(plan.getGoal().getObjective().name());
    }

    return dto;
  }

  default List<PostCommentResponse> toCommentList(List<PostComment> comments) {
    if (comments == null) return List.of();
    return comments.stream()
        .filter(c -> !c.getIsDeleted()) // chỉ lấy comment is deleted = false
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
        .toList();
  }
}
