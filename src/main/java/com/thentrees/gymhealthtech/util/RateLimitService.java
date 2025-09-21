package com.thentrees.gymhealthtech.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {
  @Value("${app.rate-limit.otp-requests:3}")
  private int otpRequestsLimit;

  @Value("${app.rate-limit.verify-attempts:5}")
  private int verifyAttemptsLimit;

  @Value("${app.rate-limit.window-minutes:15}")
  private int windowMinutes;

  private final ConcurrentHashMap<String, Bucket> otpBuckets = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Bucket> verifyBuckets = new ConcurrentHashMap<>();

  public boolean allowOtpRequest(String email) {
    return getBucket(otpBuckets, email, otpRequestsLimit).tryConsume(1);
  }

  public boolean allowVerifyAttempt(String email) {
    return getBucket(verifyBuckets, email, verifyAttemptsLimit).tryConsume(1);
  }

  private Bucket getBucket(ConcurrentHashMap<String, Bucket> buckets, String key, int limit) {
    return buckets.computeIfAbsent(
        key,
        k -> {
          Bandwidth bandwidth =
              Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(windowMinutes)));
          return Bucket.builder().addLimit(bandwidth).build();
        });
  }
}
