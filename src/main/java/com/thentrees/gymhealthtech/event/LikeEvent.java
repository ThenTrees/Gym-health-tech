package com.thentrees.gymhealthtech.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class LikeEvent {
  private UUID userId;
  private UUID postId;

  public LikeEvent(UUID userId, UUID postId) {
    this.userId = userId;
    this.postId = postId;
  }
}
