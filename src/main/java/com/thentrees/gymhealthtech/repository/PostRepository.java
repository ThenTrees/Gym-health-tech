package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Post;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, UUID> {

  @EntityGraph(attributePaths = {
    "user", "user.profile", "plan", "comments", "comments.user", "comments.user.profile"
  })
  Optional<Post> findById(UUID id);

  @Query("""
      SELECT DISTINCT p FROM Post p
      LEFT JOIN FETCH p.user u
      LEFT JOIN FETCH u.profile
      LEFT JOIN FETCH p.plan
      WHERE p.isDeleted = false
      ORDER BY p.createdAt DESC
      """)
  List<Post> findAllWithRelations();

  @Modifying
  @Transactional
  @Query("update Post p set p.commentsCount = p.commentsCount + 1 where p.id = :id")
  void incrementCommentsCount(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query("update Post p set p.likesCount = p.likesCount + 1 where p.id = :id")
  void incrementLikesCount(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query(
      "update Post p set p.commentsCount = case when p.commentsCount > 0 then p.commentsCount - 1 else 0 end where p.id = :id")
  void decrementCommentsCount(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query(
      "update Post p set p.likesCount = case when p.likesCount > 0 then p.likesCount - 1 else 0 end where p.id = :id")
  void decrementLikesCount(@Param("id") UUID id);
}
