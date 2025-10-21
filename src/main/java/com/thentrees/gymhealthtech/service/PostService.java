package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreatePostRequest;
import com.thentrees.gymhealthtech.dto.response.PostResponse;

public interface PostService {
  PostResponse createPost(CreatePostRequest request);

  PostResponse getPostDetail(String postId);
}
