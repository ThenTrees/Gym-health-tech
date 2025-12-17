package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.SessionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
  private LocalDateTime startedAt = LocalDateTime.now();

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SessionStatus status = SessionStatus.IN_PROGRESS;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "session_rpe")
  private Integer sessionRpe; // Rate of Perceived Exertion (1-10)

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SessionSet> sessionSets;
}
