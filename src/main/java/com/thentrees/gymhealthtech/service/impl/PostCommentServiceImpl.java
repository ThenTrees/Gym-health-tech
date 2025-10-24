package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.constant.S3Constant;
import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import com.thentrees.gymhealthtech.event.CommentCreatedEvent;
import com.thentrees.gymhealthtech.event.CommentDeletedEvent;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.PostCommentMapper;
import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.PostComment;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PostCommentRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.service.PostCommentService;
import com.thentrees.gymhealthtech.service.UserService;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

  private final PostCommentRepository commentRepository;
  private final PostRepository postRepository;
  private final PostCommentMapper postCommentMapper;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final FileValidator fileValidator;
  private final S3Util s3Util;
  private final UserService userService;

  @Transactional
  @Override
  public PostCommentResponse createPostComment(CreateCommentRequest request, MultipartFile file) {
    Optional<Post> post = postRepository.findById(UUID.fromString(request.getPostId()));
    if (post.isEmpty()) {
      throw new ResourceNotFoundException("Post not found with id: " + request.getPostId());
    }

    PostComment comment = postCommentMapper.toEntity(request, postRepository);

    if (request.getParentCommentId() != null) {
      PostComment parent =
          commentRepository
              .findById(UUID.fromString(request.getParentCommentId()))
              .orElseThrow(
                  () -> new ResourceNotFoundException("PostComment", request.getParentCommentId()));
      comment.setParentComment(parent);
    }
    comment.setLikesCount(0);
    comment.setRepliesCount(0);
    comment.setIsActive(true);
    comment.setIsPinned(false);

    if (file != null && !file.isEmpty()) {
      String fileUrl = null;
      try {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
          fileValidator.validateImage(file);
          fileUrl = s3Util.uploadFile(file, S3Constant.S3_IMAGE_POST_FOLDER);
        } else if (contentType != null && contentType.startsWith("video/")) {
          fileValidator.validateVideo(file);
          fileUrl = s3Util.uploadFile(file, S3Constant.S3_VIDEO_FOLDER);
        } else {
          throw new IllegalArgumentException("Only image and video files are allowed");
        }
        comment.setMediaUrl(fileUrl);
      } catch (Exception e) {
        log.error("Error uploading profile image", e);
        if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
        throw new BusinessException("Failed to upload profile image", e.getMessage());
      }
    }

    PostComment saved = commentRepository.save(comment);
    applicationEventPublisher.publishEvent(new CommentCreatedEvent(comment.getPost()));
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
            .orElseThrow(
                () -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    if (!comment.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("User is not authorized to delete this comment");
    }
    comment.setIsDeleted(true);
    commentRepository.save(comment);

    applicationEventPublisher.publishEvent(new CommentDeletedEvent(comment.getPost()));

    log.info("Comment with id: {} marked as deleted", commentId);
  }

  @Transactional
  @Override
  public void deleteCommentMedia(String mediaUrl, UUID commentId, UUID userId) {
    PostComment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("PostComment", commentId.toString()));

    Post post =
        postRepository
            .findById(comment.getPost().getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Post", comment.getPost().getId().toString()));

    User user = userService.getUserById(userId);

    if (!post.getUser().getId().equals(user.getId())
        || !comment.getUser().getId().equals(user.getId())) {
      throw new AccessDeniedException("You are not allowed to delete media from this comment");
    }

    // Xoá file vật lý trên S3
    //    s3Util.deleteFileByUrl(mediaUrl);
    s3Util.deleteFileByKey(mediaUrl);

    post.setMediaUrls(null);
    postRepository.save(post);
  }
}
