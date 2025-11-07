package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "Post ID cannot be blank")
  private UUID postId;

  @NotBlank(message = "User ID cannot be blank")
  private String userId;

  private UUID parentCommentId;

  private String content;
}
