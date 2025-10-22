package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

  private final PostRepository postRepository;

  @TransactionalEventListener
  public void handleCommentCreated(CommentCreatedEvent event) {
    log.info("Handling comment created event for post id: {}", event.getPost().getId());
    UUID postId = event.getPost().getId();
    Post post = postRepository.findById(postId).orElseThrow();
    post.setCommentsCount(post.getCommentsCount() + 1);
    postRepository.save(post);
  }

  @TransactionalEventListener
  public void handleCommentDeleted(CommentDeletedEvent event) {
    log.info("Handling comment deleted event for post id: {}", event.getPost().getId());
    Post post = event.getPost();
    post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
    postRepository.save(post);
  }
}
