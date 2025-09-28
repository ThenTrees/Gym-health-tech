package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.ExperienceLevel;
import com.thentrees.gymhealthtech.common.GenderType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
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
  @Column(name = "experience_level")
  private ExperienceLevel experienceLevel;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    UserProfile that = (UserProfile) o;
    return Objects.equals(user, that.user)
        && Objects.equals(fullName, that.fullName)
        && gender == that.gender
        && Objects.equals(age, that.age)
        && Objects.equals(heightCm, that.heightCm)
        && Objects.equals(weightKg, that.weightKg)
        && Objects.equals(bmi, that.bmi)
        && Objects.equals(healthNotes, that.healthNotes)
        && Objects.equals(timezone, that.timezone)
        && Objects.equals(unitWeight, that.unitWeight)
        && Objects.equals(unitLength, that.unitLength);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        user,
        fullName,
        gender,
        age,
        heightCm,
        weightKg,
        bmi,
        healthNotes,
        timezone,
        unitWeight,
        unitLength);
  }
}
