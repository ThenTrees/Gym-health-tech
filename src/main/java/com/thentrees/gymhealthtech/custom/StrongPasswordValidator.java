package com.thentrees.gymhealthtech.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
  private static final Pattern STRONG_PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
  }
}
