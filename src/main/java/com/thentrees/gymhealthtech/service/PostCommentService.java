package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import java.util.List;

public interface PostCommentService {
  PostCommentResponse createPostComment(CreateCommentRequest request);

  List<PostCommentResponse> getPostComments(String postId);
}
