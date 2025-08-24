package com.thentrees.gymhealthtech.common;

public enum UserStatus {
  ACTIVE("active"),
  INACTIVE("inactive"),
  SUSPENDED("suspended"),
  DELETED("deleted");

  private final String displayName;

  UserStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
