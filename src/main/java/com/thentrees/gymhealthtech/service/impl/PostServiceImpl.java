package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import com.thentrees.gymhealthtech.constant.S3Constant;
import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.event.LikeEvent;
import com.thentrees.gymhealthtech.event.UnLikeEvent;
import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.PostMapper;
import com.thentrees.gymhealthtech.model.*;
import com.thentrees.gymhealthtech.repository.PlanDayRepository;
import com.thentrees.gymhealthtech.repository.PlanItemRepository;
import com.thentrees.gymhealthtech.repository.PlanRepository;
import com.thentrees.gymhealthtech.repository.PostLikeRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.service.PostService;
import com.thentrees.gymhealthtech.service.UserService;
import com.thentrees.gymhealthtech.util.FileValidator;
import com.thentrees.gymhealthtech.util.S3Util;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PostServiceImpl implements PostService {

  private final UserService userService;

  private final PlanRepository planRepository;
  private final PlanDayRepository planDayRepository;
  private final PlanItemRepository planItemRepository;
  private final PostRepository postRepository;
  private final PostMapper postMapper;
  private final PostLikeRepository postLikeRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final S3Util s3Util;
  private final FileValidator fileValidator;

  @Transactional
  @Override
  public PostResponse createPost(CreatePostRequest request, List<MultipartFile> files) {

    log.info("Creating post for user id: {}", request.getUserId());
    User user = userService.getUserById(UUID.fromString(request.getUserId()));

    Plan plan = null;
    if (request.getPlanId() != null) {
      plan =
          planRepository
              .findById(UUID.fromString(request.getPlanId()))
              .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));
    }

    Post post = mapToPostEntity(request, user, plan);

    List<String> mediaUrls = new ArrayList<>();

    if (files != null && !files.isEmpty()) {
      for (MultipartFile file : files) {
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
          mediaUrls.add(fileUrl);
        } catch (Exception e) {
          log.error("Error uploading profile image", e);
          if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
          throw new BusinessException("Failed to upload profile image", e.getMessage());
        }
      }
    }

    post.setMediaUrls(mediaUrls);
    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public PostResponse getPostDetail(String postId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    return postMapper.toResponse(post);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PostResponse> getAllPosts() {
    log.info("Fetching all posts");
    List<Post> posts = postRepository.findAll();
    return posts.stream().map(postMapper::toResponse).toList();
  }

  @Transactional
  @Override
  public void toggleLike(UUID postId, UUID userId) {
    log.info("Toggling like for post: {} by user: {}", postId, userId);
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    User user = userService.getUserById(userId);

    PostLike postLikeExists = postLikeRepository.findByPostAndUser(post, user);

    if (postLikeExists == null) {
      PostLike postLike = PostLike.builder().post(post).user(user).build();
      postLikeRepository.save(postLike);
      eventPublisher.publishEvent(new LikeEvent(userId, postId));
    } else {
      postLikeRepository.delete(postLikeExists);
      eventPublisher.publishEvent(new UnLikeEvent(userId, postId));
    }
  }

  @Transactional
  @Override
  public PostResponse toggleSave(String postId, String userId) {
    log.info("Toggling save for post: {} by user: {}", postId, userId);
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

    // TODO: Implement proper save tracking with PostSave entity
    // For now, just increment/decrement the counter
    int currentSaves = post.getSavesCount();
    if (currentSaves % 2 == 0) {
      post.setSavesCount(currentSaves + 1);
    } else {
      post.setSavesCount(Math.max(0, currentSaves - 1));
    }

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Transactional
  @Override
  public PostResponse sharePost(String postId, String userId) {
    log.info("Sharing post: {} by user: {}", postId, userId);
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

    post.setSharesCount(post.getSharesCount() + 1);

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Override
  public Object getSharedPlanDetails(String planId) {
    log.info("Fetching shared plan details for planId: {}", planId);

    Plan plan =
        planRepository
            .findById(UUID.fromString(planId))
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

    // Use the existing mapper to convert Plan to response format
    return postMapper.toPlanSummary(plan);
  }

  @Transactional
  @Override
  public Object applySharedPlan(String planId, String userId) {
    log.info("Applying shared plan {} for user: {}", planId, userId);

    // Get the shared plan
    Plan sharedPlan =
        planRepository
            .findById(UUID.fromString(planId))
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

    // Get the user
    User user = userService.getUserById(UUID.fromString(userId));

    // Create a new plan for the user (clone the shared plan)
    Plan newPlan = new Plan();
    newPlan.setUser(user);
    newPlan.setGoal(sharedPlan.getGoal());
    newPlan.setTitle(sharedPlan.getTitle() + " (Shared)");
    newPlan.setSource(PlanSourceType.SHARED); // Mark as shared plan
    newPlan.setCycleWeeks(sharedPlan.getCycleWeeks());
    newPlan.setStatus(PlanStatusType.DRAFT); // New plan starts as draft
    newPlan.setDescription(sharedPlan.getDescription());
    newPlan.setNotes(sharedPlan.getNotes());
    newPlan.setEndDate(LocalDate.now().plusWeeks(sharedPlan.getCycleWeeks()));

    // Save the new plan
    newPlan = planRepository.save(newPlan);

    // Clone all plan days
    List<PlanDay> newPlanDays = new ArrayList<>();
    if (sharedPlan.getPlanDays() != null) {
      for (PlanDay originalDay : sharedPlan.getPlanDays()) {
        PlanDay newDay = new PlanDay();
        newDay.setPlan(newPlan);
        newDay.setDayIndex(originalDay.getDayIndex());
        newDay.setSplitName(originalDay.getSplitName());
        newDay.setScheduledDate(originalDay.getScheduledDate());

        newDay = planDayRepository.save(newDay);

        // Clone all plan items for this day
        List<PlanItem> newPlanItems = new ArrayList<>();
        if (originalDay.getPlanItems() != null) {
          for (PlanItem originalItem : originalDay.getPlanItems()) {
            PlanItem newItem = new PlanItem();
            newItem.setPlanDay(newDay);
            newItem.setExercise(originalItem.getExercise());
            newItem.setItemIndex(originalItem.getItemIndex());
            newItem.setPrescription(originalItem.getPrescription());
            newItem.setNotes(originalItem.getNotes());

            newPlanItems.add(planItemRepository.save(newItem));
          }
        }

        newDay.setPlanItems(newPlanItems);
        newPlanDays.add(newDay);
      }
    }

    newPlan.setPlanDays(newPlanDays);

    // Return the response with the new plan details
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Plan applied successfully");
    response.put("planId", newPlan.getId().toString());
    response.put("planTitle", newPlan.getTitle());
    response.put("status", newPlan.getStatus());

    log.info(
        "Successfully applied shared plan {} to user {}, new plan id: {}",
        planId,
        userId,
        newPlan.getId());

    return response;
  }

  private Post mapToPostEntity(CreatePostRequest request, User user, Plan plan) {
    Post post = new Post();
    post.setUser(user);
    post.setPlan(plan);
    post.setContent(request.getContent());
    post.setTags(request.getTags());
    return post;
  }

  @Transactional
  @Override
  public void deletePost(String postId, UUID currentUserId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    if (!post.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("You are not allowed to delete this post");
    }

    post.setIsDeleted(true);
    postRepository.save(post);
  }

  @Transactional
  @Override
  public PostResponse updatePost(
      String postId, CreatePostRequest request, List<MultipartFile> files, UUID currentUserId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    if (!post.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("You are not allowed to update this post");
    }

    if (request.getContent() != null && !request.getContent().equals(post.getContent())) {
      post.setContent(request.getContent());
    }

    if (request.getTags() != null && !request.getTags().equals(post.getTags())) {
      post.setTags(request.getTags());
    }

    List<String> mediaUrls = post.getMediaUrls();

    if (files != null && !files.isEmpty()) {
      for (MultipartFile file : files) {
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
          mediaUrls.add(fileUrl);
        } catch (Exception e) {
          log.error("Error uploading profile image", e);
          if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
          throw new BusinessException("Failed to upload profile image", e.getMessage());
        }
      }
      post.setMediaUrls(mediaUrls);
    }
    Post updated = postRepository.save(post);
    return postMapper.toResponse(updated);
  }

  @Transactional
  @Override
  public void deletePostMedia(UUID postId, String mediaUrl, UUID currentUserId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    User user = userService.getUserById(currentUserId);

    if (!post.getUser().getId().equals(user.getId())) {
      throw new AccessDeniedException("You are not allowed to delete media from this post");
    }

    List<String> mediaUrls = post.getMediaUrls();
    if (mediaUrls == null || !mediaUrls.contains(mediaUrl)) {
      throw new BusinessException("Media not found in post");
    }

    // Xoá file vật lý trên S3
    //    s3Util.deleteFileByUrl(mediaUrl);
    s3Util.deleteFileByKey(mediaUrl);

    // Cập nhật danh sách mediaUrls trong DB
    List<String> updatedUrls = mediaUrls.stream().filter(url -> !url.equals(mediaUrl)).toList();

    post.setMediaUrls(updatedUrls);
    postRepository.save(post);
  }
}
