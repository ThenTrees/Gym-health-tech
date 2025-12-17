package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.model.Post;
import lombok.Getter;

@Getter
public class CommentDeletedEvent {
  private final Post post;

  public CommentDeletedEvent(Post post) {
    this.post = post;
  }
}
