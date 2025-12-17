package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
  PostResponse createPost(CreatePostRequest request, List<MultipartFile> files);

  PostResponse getPostDetail(String postId);

  List<PostResponse> getAllPosts();

  void toggleLike(UUID postId, Authentication authentication);

  PostResponse toggleSave(UUID postId);

  PostResponse sharePost(UUID postId);

  Object getSharedPlanDetails(UUID planId);

  Object applySharedPlan(String planId, String userId);

  void deletePost(UUID postId, Authentication authentication);

  PostResponse updatePost(
      UUID postId, CreatePostRequest request, List<MultipartFile> files, Authentication authentication);

  void deletePostMedia(UUID postId, String mediaUrl, Authentication authentication);
}
