package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.PostMapper;
import com.thentrees.gymhealthtech.model.Plan;
import com.thentrees.gymhealthtech.model.PlanDay;
import com.thentrees.gymhealthtech.model.PlanItem;
import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PlanDayRepository;
import com.thentrees.gymhealthtech.repository.PlanItemRepository;
import com.thentrees.gymhealthtech.repository.PlanRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.service.PostService;
import com.thentrees.gymhealthtech.service.UserService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  @Override
  public PostResponse createPost(CreatePostRequest request) {

    log.info("Creating post for user id: {}", request.getUserId());
    User user = userService.getUserById(UUID.fromString(request.getUserId()));

    Plan plan = null;
    if (request.getPlanId() != null) {
      plan =
          planRepository
              .findById(UUID.fromString(request.getPlanId()))
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Plan not found with id: " + request.getPlanId()));
    }

    Post post = mapToPostEntity(request, user, plan);

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public PostResponse getPostDetail(String postId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
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
  public PostResponse toggleLike(String postId, String userId) {
    log.info("Toggling like for post: {} by user: {}", postId, userId);
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    // TODO: Implement proper like tracking with PostLike entity
    // For now, just increment/decrement the counter
    int currentLikes = post.getLikesCount();
    // Simple toggle: if odd, user already liked, so unlike. if even, like it
    if (currentLikes % 2 == 0) {
      post.setLikesCount(currentLikes + 1);
    } else {
      post.setLikesCount(Math.max(0, currentLikes - 1));
    }

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Transactional
  @Override
  public PostResponse toggleSave(String postId, String userId) {
    log.info("Toggling save for post: {} by user: {}", postId, userId);
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

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
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    post.setSharesCount(post.getSharesCount() + 1);

    Post saved = postRepository.save(post);
    return postMapper.toResponse(saved);
  }

  @Override
  public Object getSharedPlanDetails(String planId) {
    log.info("Fetching shared plan details for planId: {}", planId);
    
    Plan plan = planRepository
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
    Plan sharedPlan = planRepository
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
    
    log.info("Successfully applied shared plan {} to user {}, new plan id: {}", 
        planId, userId, newPlan.getId());
    
    return response;
  }

  private Post mapToPostEntity(CreatePostRequest request, User user, Plan plan) {
    Post post = new Post();
    post.setUser(user);
    post.setPlan(plan);
    post.setContent(request.getContent());
    post.setTags(request.getTags());
    post.setMediaUrls(request.getMediaUrls());
    post.setLikesCount(request.getLikeCount());
    post.setCommentsCount(request.getCommentCount());
    post.setSharesCount(request.getShareCount());
    post.setSavesCount(request.getSaveCount());
    return post;
  }

  @Transactional
  @Override
  public void deletePost(String postId, UUID currentUserId) {
    Post post = postRepository.findById(UUID.fromString(postId))
      .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    if (!post.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("You are not allowed to delete this post");
    }

    post.setIsDeleted(true);
    postRepository.save(post);
  }

  @Transactional
  @Override
  public PostResponse updatePost(String postId, CreatePostRequest request, UUID currentUserId) {
    Post post = postRepository.findById(UUID.fromString(postId))
      .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    if (!post.getUser().getId().equals(currentUserId)) {
      throw new AccessDeniedException("You are not allowed to update this post");
    }

    post.setContent(request.getContent());
    post.setTags(request.getTags());
    post.setMediaUrls(request.getMediaUrls());
//    post.setLikesCount(request.getLikeCount());
//    post.setCommentsCount(request.getCommentCount());
//    post.setSharesCount(request.getShareCount());
//    post.setSavesCount(request.getSaveCount());

    Post updated = postRepository.save(post);
    return postMapper.toResponse(updated);
  }
}
