package com.thentrees.gymhealthtech.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentResponse {
  private String id;
  private UserSummaryResponse user;
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
