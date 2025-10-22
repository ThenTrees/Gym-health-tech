package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.event.CommentCreatedEvent;
import com.thentrees.gymhealthtech.event.CommentDeletedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
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
  private final ApplicationEventPublisher applicationEventPublisher;
  @Transactional
  @Override
  public PostCommentResponse createPostComment(CreateCommentRequest request) {
    Optional<Post> post = postRepository.findById(UUID.fromString(request.getPostId()));
    if (post.isEmpty()) {
      throw new ResourceNotFoundException("Post not found with id: " + request.getPostId());
    }

    User user = userService.getUserById(UUID.fromString(request.getUserId()));

    PostComment comment = postCommentMapper.toEntity(request, postRepository);

    if (request.getParentCommentId() != null) {
      PostComment parent =
          commentRepository
              .findById(UUID.fromString(request.getParentCommentId()))
              .orElseThrow(() -> new RuntimeException("Parent comment not found"));
      comment.setParentComment(parent);
    }
    PostComment saved = commentRepository.save(comment);
    applicationEventPublisher.publishEvent(new CommentCreatedEvent(this, comment.getPost()));
    return postCommentMapper.toDto(saved);
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

  @Transactional
  @Override
  public void deleteCommentsByUserId(String commentId, UUID userId) {
    PostComment comment =
        commentRepository
            .findById(UUID.fromString(commentId))
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    if (!comment.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("User is not authorized to delete this comment");
    }
    comment.setIsDeleted(true);
    commentRepository.save(comment);

    applicationEventPublisher.publishEvent(new CommentDeletedEvent(comment.getPost()));

    log.info("Comment with id: {} marked as deleted", commentId);
  }
}
