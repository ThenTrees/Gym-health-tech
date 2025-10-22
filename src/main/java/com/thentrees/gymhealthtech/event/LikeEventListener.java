package com.thentrees.gymhealthtech.event;

import com.thentrees.gymhealthtech.repository.PostRepository;
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
public class LikeEventListener {

  private final PostRepository postRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleLikeToggler(LikeEvent event) {
    log.info("User {} liked the post has id: {}", event.getUserId(), event.getPostId());
    postRepository.incrementLikesCount(event.getPostId());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUnLikeToggle(UnLikeEvent event) {
    log.info("User {} unLiked the post has id: {}", event.getUserId(), event.getPostId());
    postRepository.decrementLikesCount(event.getPostId());
  }
}
