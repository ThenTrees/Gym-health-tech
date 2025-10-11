package com.thentrees.gymhealthtech.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_times")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MealTime extends BaseEntity {
  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "name")
  private String name;

  @Column(name = "name_vi")
  private String name_vi;

  @Column(name = "display_order")
  private Integer display_order = 0;

  @Column(name = "icon")
  private String icon;

  @Column(name = "default_calorie_percentage")
  private BigDecimal default_calorie_percentage;
}
