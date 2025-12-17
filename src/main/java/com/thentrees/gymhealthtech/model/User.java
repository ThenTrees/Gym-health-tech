package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.UserRole;
import com.thentrees.gymhealthtech.enums.UserStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
  @Column(name = "email", unique = true, nullable = false, columnDefinition = "citext")
  private String email;

  @Column(name = "phone", unique = true, length = 20)
  private String phone;

  @Column(name = "password_hash", nullable = true)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status = UserStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role = UserRole.USER;

  @Column(name = "email_verified", nullable = false)
  private Boolean emailVerified = false;

  @Column(name = "profile_completed", nullable = false)
  private Boolean profileCompleted = false;

  @Column(name = "is_premium", nullable = false)
  private Boolean isPremium = false;

  @Column(name = "premium_expires_at")
  private LocalDateTime premiumExpiresAt;

  // One-to-One relationship with UserProfile
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private UserProfile profile;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.getRole().getDisplayName()));
    return authorities;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.ACTIVE && Boolean.TRUE.equals(emailVerified);
  }
}
