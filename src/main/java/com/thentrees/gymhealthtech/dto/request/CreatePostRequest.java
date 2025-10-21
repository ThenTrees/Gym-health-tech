package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

  @NotBlank(message = "User ID cannot be blank")
  private String userId;

  private String planId;

  @NotBlank(message = "Content cannot be blank")
  private String content;

  private List<String> tags;

  private List<String> mediaUrls;

  @Min(value = 0, message = "Like count cannot be negative")
  @Builder.Default
  private int likeCount = 0;

  @Min(value = 0, message = "comment count cannot be negative")
  @Builder.Default
  private int commentCount = 0;

  @Min(value = 0, message = "share count cannot be negative")
  @Builder.Default
  private int shareCount = 0;

  @Min(value = 0, message = "save count cannot be negative")
  @Builder.Default
  private int saveCount = 0;
}
