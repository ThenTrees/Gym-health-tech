package com.thentrees.gymhealthtech.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
@RequiredArgsConstructor
public class CacheKeyUtils {

  private final ObjectMapper objectMapper;

  public String buildKey(String prefix, Object obj) {
    try {
      String json = objectMapper.writeValueAsString(obj);
      return prefix + DigestUtils.md5DigestAsHex(json.getBytes());
    } catch (Exception e) {
      throw new RuntimeException("Không thể tạo cache key", e);
    }
  }
}
