package com.thentrees.gymhealthtech.enums;

public enum UserStatus {
  ACTIVE("active"),
  INACTIVE("inactive"),
  SUSPENDED("suspended"),
  PENDING_VERIFICATION("pending_verification"),
  DELETED("deleted");

  private final String displayName;

  UserStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
