package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "plan_days")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanDay {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @Column(name = "day_index", nullable = false)
  private Integer dayIndex;

  @Column(name = "split_name", length = 50)
  private String splitName;

  @Column(name = "scheduled_date")
  private LocalDate scheduledDate;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @OneToMany(mappedBy = "planDay", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private List<PlanItem> planItems;
}
