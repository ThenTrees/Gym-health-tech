package com.thentrees.gymhealthtech.enums;

public enum UserRole {
  ADMIN("admin"),
  USER("user"),
  GUEST("guest");

  private final String displayName;

  UserRole(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
