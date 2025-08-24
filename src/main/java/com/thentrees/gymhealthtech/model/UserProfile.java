package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.GenderType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "full_name", length = 120)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private GenderType gender;

  @Column(name = "dob")
  private java.time.LocalDate dateOfBirth;

  @Column(name = "height_cm", precision = 5, scale = 2)
  private java.math.BigDecimal heightCm;

  @Column(name = "weight_kg", precision = 5, scale = 2)
  private java.math.BigDecimal weightKg;

  @Column(name = "bmi", precision = 5, scale = 2)
  private java.math.BigDecimal bmi;

  @Column(name = "health_notes", columnDefinition = "TEXT")
  private String healthNotes;

  @Column(name = "timezone", nullable = false, length = 64)
  private String timezone = "Asia/Ho_Chi_Minh";

  @Column(name = "unit_weight", nullable = false, length = 8)
  private String unitWeight = "kg";

  @Column(name = "unit_length", nullable = false, length = 8)
  private String unitLength = "cm";
}
