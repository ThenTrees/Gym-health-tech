package com.thentrees.gymhealthtech.exception;

import com.thentrees.gymhealthtech.constant.ErrorCodes;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BaseException {
  private final String resourceType;
  private final String resourceId;

  public ResourceNotFoundException(String resourceType, String resourceId) {
    super(
        String.format("%s with id '%s' not found", resourceType, resourceId), ErrorCodes.RESOURCE_NOT_FOUND);
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  public ResourceNotFoundException(String message) {
    super(message, ErrorCodes.RESOURCE_NOT_FOUND);
    this.resourceType = null;
    this.resourceId = null;
  }
}
