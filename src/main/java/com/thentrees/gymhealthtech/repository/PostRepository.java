package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Post;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, UUID> {

  @Modifying
  @Transactional
  @Query("update Post p set p.commentsCount = p.commentsCount + 1 where p.id = :id")
  void incrementCommentsCount(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query(
      "update Post p set p.commentsCount = case when p.commentsCount > 0 then p.commentsCount - 1 else 0 end where p.id = :id")
  void decrementCommentsCount(@Param("id") UUID id);
}
