package com.thentrees.gymhealthtech.model;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ExerciseEquimentId implements Serializable {
  private UUID exerciseId;
  private String equimentTypeCode;
}
