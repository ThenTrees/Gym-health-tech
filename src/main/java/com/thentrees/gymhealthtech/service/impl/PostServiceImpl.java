package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import com.thentrees.gymhealthtech.exception.ResourceNotFoundException;
import com.thentrees.gymhealthtech.mapper.PostMapper;
import com.thentrees.gymhealthtech.model.Plan;
import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PlanRepository;
import com.thentrees.gymhealthtech.repository.PostRepository;
import com.thentrees.gymhealthtech.service.PostService;
import com.thentrees.gymhealthtech.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final UserService userService;

  private final PlanRepository planRepository;
  private final PostRepository postRepository;
  private final PostMapper postMapper;

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

    postRepository.save(post);
    return postMapper.toResponse(post);
    //    return mapToPostResponse(post);
  }

  @Override
  public PostResponse getPostDetail(String postId) {
    Post post =
        postRepository
            .findById(UUID.fromString(postId))
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    return postMapper.toResponse(post);
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

  //  private PostResponse mapToPostResponse(Post post) {
  //    PostResponse response = new PostResponse();
  //
  //    response.setPostId(post.getId().toString());
  ////    response.setUser(post.getUser().getProfile());
  //
  //    PlanResponse plan =
  // customPlanService.getPlanDetails(post.getUser().getId(),post.getPlan().getId());
  //    response.setPlan(plan);
  //    response.setContent(post.getContent());
  //    response.setTags(post.getTags());
  //    response.setMediaUrls(post.getMediaUrls());
  //    response.setLikeCount(post.getLikesCount());
  //    response.setCommentCount(post.getCommentsCount());
  //    response.setShareCount(post.getSharesCount());
  //    response.setSaveCount(post.getSavesCount());
  //    response.setCreatedAt(post.getCreatedAt());
  //
  //    // query get all comment for the post
  //    response.setComments(post.getComments());
  //    return response;
  //  }

}
