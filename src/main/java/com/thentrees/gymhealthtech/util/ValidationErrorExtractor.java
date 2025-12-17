package com.thentrees.gymhealthtech.util;

import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * Utility class for extracting validation errors from BindingResult.
 */
public final class ValidationErrorExtractor {

  private ValidationErrorExtractor() {
    // Utility class - prevent instantiation
  }

  /**
   * Extracts field errors from a BindingResult into a Map.
   *
   * @param bindingResult the binding result containing validation errors
   * @return map of field names to error messages
   */
  public static Map<String, String> extract(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : bindingResult.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return errors;
  }
}
