package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercise_equipments")
public class ExerciseEquipment {
  @EmbeddedId private ExerciseEquipmentId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("exerciseId")
  @JoinColumn(name = "exercise_id")
  private Exercise exercise;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("equipmentTypeCode")
  @JoinColumn(name = "equipment_code")
  private Equipment equipment;
}
