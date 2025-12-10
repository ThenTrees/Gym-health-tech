package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.PlanSourceType;
import com.thentrees.gymhealthtech.enums.PlanStatusType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @OrderColumn(name = "day_index")
  private List<PlanDay> planDays = new ArrayList<>();

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  /** locked (BOOL): khóa khi đã kích hoạt để tránh sửa trực tiếp (buộc tạo revision mới) */
}
