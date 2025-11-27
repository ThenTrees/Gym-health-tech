package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.service.RateLimiterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j(topic = "RATE_LIMITER-SERVICE")
public class RateLimiterServiceImpl implements RateLimiterService {
  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  @Override
  public boolean tryConsume(String key) {
    Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
    return bucket.tryConsume(1);
  }

  private Bucket newBucket(String key) {
    Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofSeconds(10)));
    // 5 request mỗi 10 giây
    return Bucket4j.builder().addLimit(limit).build();
  }
}
