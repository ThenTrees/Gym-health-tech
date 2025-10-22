package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import java.util.List;

public interface PostService {
  PostResponse createPost(CreatePostRequest request);

  PostResponse getPostDetail(String postId);

  List<PostResponse> getAllPosts();

  PostResponse toggleLike(String postId, String userId);

  PostResponse toggleSave(String postId, String userId);

  PostResponse sharePost(String postId, String userId);

  Object getSharedPlanDetails(String planId);

  Object applySharedPlan(String planId, String userId);
}
