package com.thentrees.gymhealthtech.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "template_items")
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
  private String reps; // "10-12", "max", "30 seconds"

  @Column(name = "rest_seconds")
  private Integer restSeconds;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "progression_rule", columnDefinition = "jsonb")
  private JsonNode progressionRule; // Rules for progression over weeks
}
