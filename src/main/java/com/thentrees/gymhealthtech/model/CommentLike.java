package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "comment_likes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLike {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private String id;

  @ManyToOne
  @JoinColumn(name = "comment_id", nullable = false)
  private PostComment commentId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User userId;
}
