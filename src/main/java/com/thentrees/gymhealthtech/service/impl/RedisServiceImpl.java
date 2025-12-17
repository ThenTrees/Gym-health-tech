package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.exception.InfraRedisException;
import com.thentrees.gymhealthtech.service.RedisService;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REDIS-SERVICE")
public class RedisServiceImpl implements RedisService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void set(String key, Object value, Duration timeout) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout);
      log.info("Stored key: {} with TTL: {} seconds", key, timeout.getSeconds());
    } catch (Exception e) { // RedisException hoặc bất kỳ lỗi runtime
      log.error("Error storing key: {} in Redis", key, e);
      throw new InfraRedisException("Lỗi lưu trữ dữ liệu Redis", e);
    }
  }

  @Override
  public void set(String key, Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
      log.info("Stored key: {}", key);
    } catch (Exception e) {
      log.error("Error storing key: {} in Redis", key, e);
      throw new InfraRedisException("Lỗi lưu trữ dữ liệu Redis", e);
    }
  }

  @Override
  public Object get(String key) {
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (Exception e) {
      log.error("Error getting key: {} from Redis", key, e);
      throw new InfraRedisException("Lỗi đọc dữ liệu Redis", e);
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
      throw new InfraRedisException("Lỗi xóa dữ liệu Redis", e);
    }
  }

  @Override
  public void deletePattern(String pattern) {
    try {
      ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
      AtomicInteger deletedCount = new AtomicInteger();

      redisTemplate.execute((RedisCallback<Object>) connection -> {
        try (Cursor<byte[]> cursor = connection.scan(options)) {
          while (cursor.hasNext()) {
            byte[] keyBytes = cursor.next();
            String key = redisTemplate.getStringSerializer().deserialize(keyBytes);
            if (key != null) {
              redisTemplate.delete(key);
              deletedCount.getAndIncrement();
            }
          }
        }
        return null;
      });

      log.info("Deleted {} keys matching pattern: {}", deletedCount, pattern);

    } catch (Exception e) {
      log.error("Error deleting keys with pattern: {} from Redis", pattern, e);
      throw new InfraRedisException("Lỗi xóa dữ liệu Redis theo pattern", e);
    }
  }

  @Override
  public String getOtpKey(String email) {
    return "otp:" + email;
  }
}
