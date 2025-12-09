package com.thentrees.gymhealthtech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

  @Value("${app.aiUrl}")
  private String aiServiceBaseUrl;

  @Bean
  public WebClient webClient(WebClient.Builder webClientBuilder) {
    // config connectionProvider for timeout
    ConnectionProvider provider = ConnectionProvider.builder("ai-connection-pool")
      // timeout wait connection successfully
      .maxIdleTime(Duration.ofMinutes(10))
      .maxIdleTime(Duration.ofSeconds(30))
      .build();

    HttpClient httpClient = HttpClient.create(provider)
    // set Read and Write timeout
      .responseTimeout(Duration.ofSeconds(40));

    return webClientBuilder.baseUrl(aiServiceBaseUrl)
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
  }
}
