package com.thentrees.gymhealthtech.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class UnLikeEvent {
  private UUID userId;
  private UUID postId;

  public UnLikeEvent(UUID userId, UUID postId) {
    this.userId = userId;
    this.postId = postId;
  }
}
