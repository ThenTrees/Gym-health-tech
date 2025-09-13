package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.SessionStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_day_id")
  private PlanDay planDay;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt = OffsetDateTime.now();

  @Column(name = "ended_at")
  private OffsetDateTime endedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SessionStatus status = SessionStatus.IN_PROGRESS;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SessionSet> sessionSets;
}
