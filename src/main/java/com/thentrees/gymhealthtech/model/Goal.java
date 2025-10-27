package com.thentrees.gymhealthtech.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.thentrees.gymhealthtech.enums.GoalStatus;
import com.thentrees.gymhealthtech.enums.ObjectiveType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "goals")
public class Goal extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "objective", nullable = false)
  private ObjectiveType objective;

  @Column(name = "sessions_per_week", nullable = false)
  private Integer sessionsPerWeek;

  @Column(name = "session_minutes", nullable = false)
  private Integer sessionMinutes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "preferences", columnDefinition = "jsonb")
  private JsonNode preferences;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt = LocalDateTime.now();

  @Column(name = "ended_at")
  private LocalDate endedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private GoalStatus status;

  // Additional computed fields
  private Integer estimatedCaloriesPerSession;
  private String difficultyAssessment;
  private List<String> recommendedEquipment;
  private String healthSafetyNotes;
}
