package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_accounts")
public class OAuthAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "provider", nullable = false, length = 32)
  private String provider;

  @Column(name = "provider_user_id", nullable = false, length = 128)
  private String providerUserId;

  @Column(name = "email", columnDefinition = "citext")
  private String email;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
