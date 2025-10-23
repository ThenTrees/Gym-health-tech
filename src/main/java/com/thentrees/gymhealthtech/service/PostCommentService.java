package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.request.CreateCommentRequest;
import com.thentrees.gymhealthtech.dto.response.PostCommentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface PostCommentService {
  PostCommentResponse createPostComment(CreateCommentRequest request, MultipartFile file);

  List<PostCommentResponse> getPostComments(String postId);

  void deleteCommentsByUserId(String commentId, UUID userId);

  void deleteCommentMedia(String mediaUrl, UUID commentId, UUID userId);
}
