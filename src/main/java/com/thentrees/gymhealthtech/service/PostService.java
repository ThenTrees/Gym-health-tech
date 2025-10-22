package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import java.util.List;
import java.util.UUID;

public interface PostService {
  PostResponse createPost(CreatePostRequest request);

  PostResponse getPostDetail(String postId);

  List<PostResponse> getAllPosts();

  void toggleLike(UUID postId, UUID userId);

  PostResponse toggleSave(String postId, String userId);

  PostResponse sharePost(String postId, String userId);

  Object getSharedPlanDetails(String planId);

  Object applySharedPlan(String planId, String userId);

  void deletePost(String postId, UUID currentUserId);

  PostResponse updatePost(String postId, CreatePostRequest request, UUID currentUserId);
}
