package com.thentrees.gymhealthtech.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerateTraceId {
  public String generate() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
