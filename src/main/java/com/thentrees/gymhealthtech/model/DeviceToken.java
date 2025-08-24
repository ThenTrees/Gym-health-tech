package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.DevicePlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "device_tokens")
public class DeviceToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform", nullable = false)
  private DevicePlatform platform;

  @Column(name = "push_token", nullable = false)
  private String pushToken;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();
}
