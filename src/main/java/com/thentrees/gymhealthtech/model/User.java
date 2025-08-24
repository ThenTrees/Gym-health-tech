package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.UserRole;
import com.thentrees.gymhealthtech.common.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "phone", unique = true, length = 20)
  private String phone;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status = UserStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role = UserRole.USER;

  @Column(name = "email_verified", nullable = false)
  private Boolean emailVerified = false;

  // One-to-One relationship with UserProfile
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private UserProfile profile;
}
