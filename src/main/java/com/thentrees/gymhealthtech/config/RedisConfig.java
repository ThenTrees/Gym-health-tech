package com.thentrees.gymhealthtech.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory(RedisProperties props) {
    var cfg = new RedisStandaloneConfiguration(props.getHost(), props.getPort());
    if (props.getUsername() != null && !props.getUsername().isBlank())
      cfg.setUsername(props.getUsername());
    if (props.getPassword() != null) cfg.setPassword(RedisPassword.of(props.getPassword()));

    if (props.getSsl().isEnabled()) {
      LettuceClientConfiguration.LettuceSslClientConfigurationBuilder builder =
          LettuceClientConfiguration.builder()
              .commandTimeout(
                  props.getTimeout() != null ? props.getTimeout() : Duration.ofSeconds(2))
              .useSsl();
      return new LettuceConnectionFactory(cfg, builder.build());
    } else {
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
          LettuceClientConfiguration.builder()
              .commandTimeout(
                  props.getTimeout() != null ? props.getTimeout() : Duration.ofSeconds(2));
      return new LettuceConnectionFactory(cfg, builder.build());
    }
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
    var t = new RedisTemplate<String, Object>();
    t.setConnectionFactory(cf);
    var keySer = new org.springframework.data.redis.serializer.StringRedisSerializer();
    var valSer = new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer();
    t.setKeySerializer(keySer);
    t.setHashKeySerializer(keySer);
    t.setValueSerializer(valSer);
    t.setHashValueSerializer(valSer);
    t.afterPropertiesSet();
    return t;
  }
}
