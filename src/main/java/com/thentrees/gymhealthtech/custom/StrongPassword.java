package com.thentrees.gymhealthtech.custom;

import com.thentrees.gymhealthtech.constant.ValidationMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
  String message() default
    ValidationMessages.PASSWORD_TOO_WEAK;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
