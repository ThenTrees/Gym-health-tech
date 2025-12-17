package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.model.Post;
import lombok.Getter;

@Getter
public class CommentCreatedEvent {
  private final Post post;

  public CommentCreatedEvent(Post post) {
    this.post = post;
  }
}
