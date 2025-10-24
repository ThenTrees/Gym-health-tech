package com.thentrees.gymhealthtech.dto.request;

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
}
