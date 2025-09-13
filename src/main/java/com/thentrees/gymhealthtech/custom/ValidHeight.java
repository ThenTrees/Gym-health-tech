package com.thentrees.gymhealthtech.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidHeightValidator.class)
@Documented
public @interface ValidHeight {
  String message() default "Height must be between 50cm and 250cm";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
