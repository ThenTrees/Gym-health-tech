package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
  PostResponse createPost(CreatePostRequest request, List<MultipartFile> files);

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
