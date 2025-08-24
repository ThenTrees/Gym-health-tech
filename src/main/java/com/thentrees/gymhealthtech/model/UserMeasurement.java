package com.thentrees.gymhealthtech.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "user_measurements")
public class UserMeasurement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "measured_at", nullable = false)
  private java.time.LocalDate measuredAt;

  @Column(name = "weight_kg", precision = 5, scale = 2)
  private java.math.BigDecimal weightKg;

  @Column(name = "bodyfat_pct", precision = 4, scale = 1)
  private java.math.BigDecimal bodyfatPct;

  @Column(name = "waist_cm", precision = 5, scale = 2)
  private java.math.BigDecimal waistCm;

  @Column(name = "hip_cm", precision = 5, scale = 2)
  private java.math.BigDecimal hipCm;

  @Column(name = "chest_cm", precision = 5, scale = 2)
  private java.math.BigDecimal chestCm;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;
}
