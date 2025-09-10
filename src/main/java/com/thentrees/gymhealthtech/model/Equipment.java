package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

  @Column(name = "image_url")
  private String imageUrl;

  @OneToMany(mappedBy = "exerciseCategory", cascade = CascadeType.ALL, orphanRemoval = false)
  private Set<Exercise> exercises = new HashSet<>();
}
