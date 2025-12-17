package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "post_comments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostComment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private PostComment parentComment;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "media_url", columnDefinition = "varchar(500)")
  private String mediaUrl;

  @Column(name = "likes_count", nullable = false)
  private Integer likesCount;

  @Column(name = "replies_count", nullable = false)
  private Integer repliesCount;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "is_pinned", nullable = false)
  private Boolean isPinned;

  // Children list
  @OneToMany(
      mappedBy = "parentComment",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<PostComment> replies;
}
