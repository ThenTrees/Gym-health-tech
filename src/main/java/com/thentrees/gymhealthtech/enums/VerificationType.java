package com.thentrees.gymhealthtech.enums;

public enum VerificationType {
  EMAIL("email"),
  PHONE("phone"),
  PASSWORD_RESET("password_reset"),
  TWO_FACTOR_AUTHENTICATION("two_factor_authentication");

  private final String displayName;

  VerificationType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
