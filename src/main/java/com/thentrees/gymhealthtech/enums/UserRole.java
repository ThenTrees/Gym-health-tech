package com.thentrees.gymhealthtech.enums;

public enum UserRole {
  ADMIN("ADMIN"),
  USER("USER"),
  GUEST("GUEST");

  private final String displayName;

  UserRole(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
