package com.thentrees.gymhealthtech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thentrees.gymhealthtech.enums.FitnessLevel;
import com.thentrees.gymhealthtech.enums.GenderType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity implements Serializable {

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @Column(name = "full_name", length = 120)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private GenderType gender;

  @Column(name = "age")
  private Integer age;

  @Column(name = "height_cm", precision = 5, scale = 2)
  private BigDecimal heightCm;

  @Column(name = "weight_kg", precision = 5, scale = 2)
  private BigDecimal weightKg;

  @Column(name = "bmi", precision = 5, scale = 2)
  private BigDecimal bmi;

  @Column(name = "avatar_url", length = 512)
  private String avatarUrl;

  @Column(name = "health_notes", columnDefinition = "TEXT")
  private String healthNotes;

  @Column(name = "timezone", nullable = false, length = 64)
  @Builder.Default
  private String timezone = "Asia/Ho_Chi_Minh";

  @Column(name = "unit_weight", nullable = false, length = 8)
  @Builder.Default
  private String unitWeight = "kg";

  @Column(name = "unit_length", nullable = false, length = 8)
  @Builder.Default
  private String unitLength = "cm";

  @Enumerated(EnumType.STRING)
  @Column(name = "fitness_level")
  private FitnessLevel fitnessLevel;

}
