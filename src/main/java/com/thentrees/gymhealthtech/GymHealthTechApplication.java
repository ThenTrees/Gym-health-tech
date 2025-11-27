package com.thentrees.gymhealthtech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class GymHealthTechApplication {

  public static void main(String[] args) {
    SpringApplication.run(GymHealthTechApplication.class, args);
  }
}
