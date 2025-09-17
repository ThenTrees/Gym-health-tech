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
@Table(name = "session_sets")
public class SessionSet extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(name = "set_index", nullable = false)
  private Integer setIndex;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "plan_item_id")
  private PlanItem planItem;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "planned", columnDefinition = "jsonb")
  private JsonNode planned;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "actual", columnDefinition = "jsonb")
  private JsonNode actual;
}
