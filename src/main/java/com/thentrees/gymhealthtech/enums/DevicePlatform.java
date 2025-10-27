package com.thentrees.gymhealthtech.enums;

public enum DevicePlatform {
  ANDROID("android"),
  IOS("ios"),
  WEB("web");

  private final String displayName;

  DevicePlatform(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
