package com.thentrees.gymhealthtech.service;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
  boolean tryConsume(String key);
}
