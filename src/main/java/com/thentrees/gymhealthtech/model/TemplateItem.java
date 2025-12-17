package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "template_items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_day_id", nullable = false)
  private TemplateDay templateDay;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(name = "item_order", nullable = false)
  private Integer itemOrder;

  @Column(name = "sets", nullable = false)
  private Integer sets;

  @Column(name = "reps", length = 50)
  private Integer reps; // "10-12", "max", "30 seconds"

  @Column(name = "rest_seconds")
  private Integer restSeconds;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;
}
