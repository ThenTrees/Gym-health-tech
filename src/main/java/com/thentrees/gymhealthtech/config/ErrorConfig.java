package com.thentrees.gymhealthtech.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration class to customize error attributes for the application.
 * This class modifies the default error response structure to include additional
 * fields such as success, timestamp, and traceId, while removing unnecessary fields.
 * Solutions: https://www.baeldung.com/spring-boot-custom-error-page
 */

@Configuration
@RequiredArgsConstructor
public class ErrorConfig {

  @Bean
  public ErrorAttributes errorAttributes() {
    return new CustomErrorAttributes();
  }

  public static class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
      Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

      // Customize error response for unhandled exceptions
      errorAttributes.put("success", false);
      errorAttributes.put("timestamp", OffsetDateTime.now());
      errorAttributes.put("traceId", generateTraceId());

      // Remove some default Spring Boot error attributes
      errorAttributes.remove("trace");

      return errorAttributes;
    }
  }
  private static String generateTraceId() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
