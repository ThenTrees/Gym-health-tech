package com.thentrees.gymhealthtech.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ExerciseMuscleId implements Serializable {
  private UUID exerciseId;
  private String muscleCode;
}
