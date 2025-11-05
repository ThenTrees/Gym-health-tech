package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "equipments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Equipment {

  @Id
  @Column(name = "code", length = 32)
  private String code;

  @Column(name = "name", nullable = false, length = 64)
  private String name;

  @Column(name = "image_url")
  private String imageUrl;
}
