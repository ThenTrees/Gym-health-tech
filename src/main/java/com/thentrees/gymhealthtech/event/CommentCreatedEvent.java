package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.model.Post;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CommentCreatedEvent extends ApplicationEvent {
  private final Post post;

  public CommentCreatedEvent(Object source,Post post) {
    super(source);
    this.post = post;
  }
}
