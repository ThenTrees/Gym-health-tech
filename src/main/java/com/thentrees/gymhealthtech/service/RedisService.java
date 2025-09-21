package com.thentrees.gymhealthtech.service;

import java.time.Duration;

public interface RedisService {

  void set(String key, Object value, Duration timeout);

  Object get(String key);

  boolean delete(String key);

  String getOtpKey(String email);
}
