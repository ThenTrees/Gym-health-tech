package com.thentrees.gymhealthtech.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "GymHealthTech API",
            description =
                "REST API for GymHealthTech application - A comprehensive fitness and health tracking platform",
            version = "v1.0.0",
            contact =
                @Contact(
                    name = "GymHealthTech Team",
                    email = "support@gymhealthtech.com",
                    url = "http://18.139.208.5:8080/"),
            license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")),
    servers = {
      @Server(description = "Development Server", url = "http://18.139.208.5:8080"),
    })
public class OpenApiConfig {
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(
            new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
  }

  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
  }
}
