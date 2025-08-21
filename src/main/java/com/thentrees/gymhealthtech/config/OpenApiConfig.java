package com.thentrees.gymhealthtech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI api() {
    return new OpenAPI().info(new Info()
      .title("API for Gym Health Tech System")
      .description("API mô tả các chức năng của hệ thống")
      .version("1.0.0")
      .license(new License().name("Apache 2.0").url("http://springdoc.org"))
      .termsOfService("https://example.com/terms")
      .contact(new Contact()
        .name("Support Team")
        .email("thentrees@gmail.com")));
  }

  @Bean
  GroupedOpenApi publicApis() {
    return GroupedOpenApi.builder()
      .group("public")
      .pathsToMatch("/api/**")
      .pathsToExclude("/api/internal/**")
      .build();
  }

  @Bean
  GroupedOpenApi adminApis() {
    return GroupedOpenApi.builder()
      .group("admin")
      .pathsToMatch("/admin/**")
      .build();
  }
}
