package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "receiver_id", nullable = false)
  private UUID receiverId;

  private String title;
  private String content;

  @Enumerated(EnumType.STRING)
  private NotificationType type; // LIKE, COMMENT, FOLLOW, SYSTEM, etc.

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
