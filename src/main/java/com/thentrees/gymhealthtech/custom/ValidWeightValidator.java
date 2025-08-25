package com.thentrees.gymhealthtech.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

class ValidWeightValidator implements ConstraintValidator<ValidWeight, Number> {
  @Override
  public boolean isValid(Number weight, ConstraintValidatorContext context) {
    if (weight == null) {
      return true;
    } // Let @NotNull handle null values
    double value = weight.doubleValue();
    return value >= 20.0 && value <= 400.0;
  }
}
