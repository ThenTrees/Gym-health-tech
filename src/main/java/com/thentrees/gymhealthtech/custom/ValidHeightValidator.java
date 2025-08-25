package com.thentrees.gymhealthtech.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

class ValidHeightValidator implements ConstraintValidator<ValidHeight, Number> {
  @Override
  public boolean isValid(Number height, ConstraintValidatorContext context) {
    if (height == null) {
      return true;
    } // Let @NotNull handle null values
    double value = height.doubleValue();
    return value >= 50.0 && value <= 250.0;
  }
}
