package com.thentrees.gymhealthtech.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PostResponse {
  private String postId;
  private UserSummaryResponse user;
  private String planId;
  private PlanSummaryResponse plan; // Add plan details
  private String content;
  private List<String> tags;
  private List<String> mediaUrls;

  private int likesCount;
  private int commentsCount;
  private int sharesCount;
  private int savesCount;

  private LocalDateTime createdAt;
  // commented out for future use
  private List<PostCommentResponse> comments;
}
