package com.thentrees.gymhealthtech.custom;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidWeightValidator.class)
@Documented
public @interface ValidWeight {
  String message() default "Weight must be between 20kg and 400kg";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
