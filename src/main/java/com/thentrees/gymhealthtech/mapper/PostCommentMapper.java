package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.dto.response.UserSummaryResponse;
import com.thentrees.gymhealthtech.mapper.helper.PostCommentMapperHelper;
import com.thentrees.gymhealthtech.model.PostComment;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PostRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {PostCommentMapperHelper.class})
public interface PostCommentMapper {

  @Mapping(target = "user", expression = "java(toUserSummary(comment.getUser()))")
  @Mapping(target = "replies", expression = "java(toReplyList(comment.getReplies()))")
  PostCommentResponse toDto(PostComment comment);

  //  @Mapping(target = "post", expression = "java(mapToPost(dto.getPostId(), postRepository))")
  @Mapping(target = "post", source = "postId", qualifiedByName = "mapPost")
  @Mapping(target = "user", source = "userId", qualifiedByName = "mapUser")
  @Mapping(target = "parentComment", source = "parentCommentId", qualifiedByName = "mapParent")
  PostComment toEntity(CreateCommentRequest dto, @Context PostRepository postRepository);

  default UserSummaryResponse toUserSummary(User user) {
    if (user == null) return null;
    return UserSummaryResponse.builder()
        .id(user.getId().toString())
        .username(user.getProfile().getFullName())
        .avatarUrl(user.getProfile().getAvatarUrl())
        .build();
  }

  default List<PostCommentResponse> toReplyList(List<PostComment> replies) {
    if (replies == null) return List.of();
    return replies.stream().map(this::toDto).toList();
  }
}
