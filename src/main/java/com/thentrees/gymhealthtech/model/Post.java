package com.thentrees.gymhealthtech.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id")
  private Plan plan;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Type(JsonBinaryType.class)
  @Column(columnDefinition = "jsonb")
  private List<String> tags;

  @Type(JsonBinaryType.class)
  @Column(name = "media_urls", columnDefinition = "jsonb")
  private List<String> mediaUrls;

  @Column(name = "like_count")
  private Integer likesCount;

  @Column(name = "comment_count")
  private Integer commentsCount;

  @Column(name = "share_count")
  private Integer sharesCount;

  @Column(name = "save_count")
  private Integer savesCount;

  @OneToMany(
      mappedBy = "post",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<PostComment> comments;
}
