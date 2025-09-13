package com.thentrees.gymhealthtech.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GenerateTraceId {
  public String generate() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
