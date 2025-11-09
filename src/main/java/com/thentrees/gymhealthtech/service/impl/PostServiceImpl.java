package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.constant.ErrorMessages;
import com.thentrees.gymhealthtech.constant.S3Constant;
import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
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
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j(topic = "POST-SERVICE")
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();
    Plan plan = getPlanIfExists(request.getPlanId());
    Post post = mapToPostEntity(request, user, plan);
    post.setLikesCount(0);
    post.setCommentsCount(0);
    post.setSharesCount(0);
    post.setSavesCount(0);

    List<String> mediaUrls = uploadMediaFiles(files);
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
    List<Post> posts = postRepository.findAll().stream().filter(p -> !p.getIsDeleted())
      .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
      .toList();
    return posts.stream().map(postMapper::toResponse).toList();
  }

  @Transactional
  @Override
  public void toggleLike(UUID postId, Authentication authentication) {

    User user = (User) authentication.getPrincipal();

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    PostLike postLikeExists = postLikeRepository.findByPostAndUser(post, user);

    if (postLikeExists == null) {
      PostLike postLike = PostLike.builder().post(post).user(user).build();
      postLikeRepository.save(postLike);
      eventPublisher.publishEvent(new LikeEvent(user.getId(), postId));
    } else {
      postLikeRepository.delete(postLikeExists);
      eventPublisher.publishEvent(new UnLikeEvent(user.getId(), postId));
    }
  }

  @Transactional
  @Override
  public PostResponse toggleSave(UUID postId) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

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
  public PostResponse sharePost(UUID postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    post.setSharesCount(post.getSharesCount() + 1);

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Override
  public Object getSharedPlanDetails(UUID planId) {
    log.info("Fetching shared plan details for planId: {}", planId);

    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));

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
            .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));

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

  @Override
  public void deletePost(UUID postId, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    if (!post.getUser().getId().equals(user.getId())) {
      throw new AccessDeniedException(ErrorMessages.NOT_ALLOWED_DELETE_POST);
    }

    post.setIsDeleted(true);
    postRepository.save(post);
  }

  @Transactional
  @Override
  public PostResponse updatePost(
      UUID postId, CreatePostRequest request, List<MultipartFile> files, Authentication authentication) {

    User user = (User) authentication.getPrincipal();

    Post post = getPostOwnedByUser(postId, user);

    updatePostFields(post, request);

    if (files != null && !files.isEmpty()) {
      List<String> updatedMediaUrls = new ArrayList<>(post.getMediaUrls());
      updatedMediaUrls.addAll(uploadMediaFiles(files));
      post.setMediaUrls(updatedMediaUrls);
    }

    Post updated = postRepository.save(post);
    return postMapper.toResponse(updated);
  }

  @Override
  public void deletePostMedia(UUID postId, String mediaUrl, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    if (!post.getUser().getId().equals(user.getId())) {
      throw new AccessDeniedException(ErrorMessages.NOT_ALLOWED_DELETE_MEDIA_POST);
    }

    List<String> mediaUrls = post.getMediaUrls();
    if (mediaUrls == null || !mediaUrls.contains(mediaUrl)) {
      throw new BusinessException("Media not found in post");
    }

    s3Util.deleteFileByKey(mediaUrl);
    List<String> updatedUrls = mediaUrls.stream().filter(url -> !url.equals(mediaUrl)).toList();
    post.setMediaUrls(updatedUrls);
    postRepository.save(post);
  }

  private Plan getPlanIfExists(String planId) {
    if (planId == null) return null;

    return planRepository.findById(UUID.fromString(planId))
      .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
  }

  private List<String> uploadMediaFiles(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) return Collections.emptyList();

    List<String> mediaUrls = new ArrayList<>();

    for (MultipartFile file : files) {
      String fileUrl = null;
      try {
        fileUrl = uploadSingleFile(file);
        mediaUrls.add(fileUrl);
      } catch (Exception e) {
        log.error("Error uploading media file: {}", file.getOriginalFilename(), e);
        if (fileUrl != null) s3Util.deleteFileByUrl(fileUrl);
        throw new BusinessException("Failed to upload media file", e.getMessage());
      }
    }

    return mediaUrls;
  }

  private String uploadSingleFile(MultipartFile file) {
    String contentType = file.getContentType();

    if (contentType == null)
      throw new IllegalArgumentException("File type not supported");

    if (contentType.startsWith("image/")) {
      fileValidator.validateImage(file);
      return s3Util.uploadFile(file, S3Constant.S3_IMAGE_POST_FOLDER);
    }

    if (contentType.startsWith("video/")) {
      fileValidator.validateVideo(file);
      return s3Util.uploadFile(file, S3Constant.S3_VIDEO_FOLDER);
    }

    throw new IllegalArgumentException("Only image and video files are allowed");
  }

  private Post getPostOwnedByUser(UUID postId, User user) {
    Post post = postRepository.findById(postId)
      .orElseThrow(() -> new ResourceNotFoundException("Post", postId.toString()));

    if (!post.getUser().getId().equals(user.getId())) {
      throw new AccessDeniedException("You are not allowed to update this post");
    }
    return post;
  }
  private void updatePostFields(Post post, CreatePostRequest request) {
    if (request.getContent() != null && !request.getContent().equals(post.getContent())) {
      post.setContent(request.getContent());
    }

    if (request.getTags() != null && !request.getTags().equals(post.getTags())) {
      post.setTags(request.getTags());
    }
  }


}
