package com.thentrees.gymhealthtech.common;

public enum GenderType {
  MALE("male"),
  FEMALE("female"),
  OTHER("other"),;

  private final String displayName;


  GenderType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
