package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

  private final PostRepository postRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCommentCreated(CommentCreatedEvent event) {
    UUID postId = event.getPost().getId();
    log.info("Incrementing comments count for post id: {}", postId);
    postRepository.incrementCommentsCount(postId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCommentDeleted(CommentDeletedEvent event) {
    UUID postId = event.getPost().getId();
    log.info("Decrementing comments count for post id: {}", postId);
    postRepository.decrementCommentsCount(postId);
  }
}
