package com.thentrees.gymhealthtech.mapper.helper;

import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.PostComment;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PostCommentRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostCommentMapperHelper {

  private final PostRepository postRepository;
  private final PostCommentRepository postCommentRepository;
  private final UserRepository userRepository;

  @Named("mapPost")
  public Post mapPost(String postId) {
    if (postId == null) return null;
    return postRepository
        .findById(UUID.fromString(postId))
        .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
  }

  @Named("mapParent")
  public PostComment mapParent(String parentCommentId) {
    if (parentCommentId == null) return null;
    return postCommentRepository
        .findById(UUID.fromString(parentCommentId))
        .orElseThrow(() -> new RuntimeException("Parent comment not found: " + parentCommentId));
  }

  @Named("mapUser")
  public User mapUser(String userId) {
    if (userId == null) return null;
    return userRepository
        .findById(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Parent comment not found: " + userId));
  }
}
