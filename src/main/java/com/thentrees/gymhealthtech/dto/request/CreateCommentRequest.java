package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "Post ID cannot be blank")
  private String postId;

  @NotBlank(message = "User ID cannot be blank")
  private String userId;

  private String parentCommentId;

  private String content;
  private String mediaUrl;

  @Builder.Default private Integer likeCount = 0;
  @Builder.Default private Integer replyCount = 0;
  @Builder.Default private Boolean isActive = true;
  @Builder.Default private Boolean isPinned = false;
}
