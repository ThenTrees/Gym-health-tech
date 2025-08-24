package com.thentrees.gymhealthtech.util;

import lombok.Getter;

@Getter
public class ResourceAlreadyExists {
  public static String formatMessage(String resourceName, String fieldName, Object fieldValue) {
    return String.format("%s with %s '%s' already exists.", resourceName, fieldName, fieldValue);
  }
}
