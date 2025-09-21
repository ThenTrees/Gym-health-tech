package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.service.RedisService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {
  @Autowired private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void set(String key, Object value, Duration timeout) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout);
      log.info("Stored key: {} with TTL: {} seconds", key, timeout.getSeconds());
    } catch (Exception e) {
      log.error("Error storing key: {} in Redis", key, e);
      throw new RuntimeException("Lỗi lưu trữ dữ liệu", e);
    }
  }

  @Override
  public Object get(String key) {
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (Exception e) {
      log.error("Error getting key: {} from Redis", key, e);
      return null;
    }
  }

  @Override
  public boolean delete(String key) {
    try {
      Boolean result = redisTemplate.delete(key);
      log.info("Deleted key: {} from Redis", key);
      return result != null && result;
    } catch (Exception e) {
      log.error("Error deleting key: {} from Redis", key, e);
      return false;
    }
  }

  @Override
  public String getOtpKey(String email) {
    return "otp:" + email;
  }
}
