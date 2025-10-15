package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.PlanSourceType;
import com.thentrees.gymhealthtech.common.PlanStatusType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "plans")
public class Plan extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "goal_id")
  private Goal goal;

  @Column(name = "title", length = 120)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false)
  private PlanSourceType source = PlanSourceType.AI;

  @Column(name = "cycle_weeks", nullable = false)
  private Integer cycleWeeks = 4;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PlanStatusType status = PlanStatusType.DRAFT;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<PlanDay> planDays;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  /** locked (BOOL): khóa khi đã kích hoạt để tránh sửa trực tiếp (buộc tạo revision mới) */
}
