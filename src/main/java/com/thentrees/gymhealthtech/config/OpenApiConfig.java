package com.thentrees.gymhealthtech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("FitnessApp API")
                .description("Gym & Health Tracking API - Powered by Spring Boot and OpenAPI 3")
                .version("v1.0.0")
                .contact(
                    new Contact()
                        .name("Thentrees")
                        .email("thientri.tran@gmail.com")
                        .url("https://gym-health-tech.com"))
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
  }
}
