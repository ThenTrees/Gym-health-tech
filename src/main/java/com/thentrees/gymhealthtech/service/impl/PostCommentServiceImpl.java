package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.PostCommentMapper;
import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.PostComment;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PostCommentRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.service.PostCommentService;
import com.thentrees.gymhealthtech.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

  private final PostCommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserService userService;
  private final PostCommentMapper postCommentMapper;

  @Override
  public PostCommentResponse createPostComment(CreateCommentRequest request) {
    Optional<Post> post = postRepository.findById(UUID.fromString(request.getPostId()));
    if (post.isEmpty()) {
      throw new ResourceNotFoundException("Post not found with id: " + request.getPostId());
    }

    User user = userService.getUserById(UUID.fromString(request.getUserId()));

    //    PostComment comment = PostComment.builder()
    //      .post(post.get())
    //      .content(request.getContent())
    //      .user(user)
    //      .content(request.getContent())
    //      .mediaUrl(request.getMediaUrl())
    //      .likesCount(request.getLikeCount())
    //      .repliesCount(request.getReplyCount())
    //      .isActive(request.getIsActive())
    //      .isPinned(request.getIsPinned())
    //      .build();

    PostComment comment = postCommentMapper.toEntity(request, postRepository);

    if (request.getParentCommentId() != null) {
      PostComment parent =
          commentRepository
              .findById(UUID.fromString(request.getParentCommentId()))
              .orElseThrow(() -> new RuntimeException("Parent comment not found"));
      comment.setParentComment(parent);
    }
    commentRepository.save(comment);

    return postCommentMapper.toDto(comment);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PostCommentResponse> getPostComments(String postId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new RuntimeException("Post not found"));
    List<PostComment> comments = commentRepository.findByPostAndParentCommentIsNull(post);
    return comments.stream().map(postCommentMapper::toDto).toList();
  }
}
