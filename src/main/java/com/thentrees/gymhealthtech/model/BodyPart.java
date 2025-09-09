package com.thentrees.gymhealthtech.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "body_parts")
public class BodyPart {
  @Id private String code; // CHEST, BACK, ...
  private String name;

  @OneToMany(mappedBy = "bodyPart")
  private List<Muscle> muscles;
}
