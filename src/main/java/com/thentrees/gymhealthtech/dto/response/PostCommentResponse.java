package com.thentrees.gymhealthtech.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCommentResponse {
  private String id;
  private UserSummaryDTO user;
  private String content;
  private String mediaUrl;
  private Integer likesCount;
  private Integer repliesCount;
  private Boolean isPinned;
  private Boolean isActive;
  private Boolean isDeleted;
  private LocalDateTime createdAt;
  private List<PostCommentResponse> replies; // đệ quy
  private String postId; // chỉ giữ id, KHÔNG giữ Post object
}
