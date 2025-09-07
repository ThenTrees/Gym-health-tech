package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "equipment_types")
public class EquipmentType {

  @Id
  @Column(name = "code", length = 32)
  private String code;

  @Column(name = "name", nullable = false, length = 64)
  private String name;
}
