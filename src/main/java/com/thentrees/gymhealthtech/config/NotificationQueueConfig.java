package com.thentrees.gymhealthtech.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationQueueConfig {
  public static final String EXCHANGE_NAME = "notification.exchange";
  public static final String QUEUE_NAME = "notification.queue";
  public static final String DLQ_NAME = "notification.dlq"; // Dead Letter Queue (retry)
  public static final String ROUTING_KEY = "notification.key";

  /** Khai báo exchange - nơi mà publisher sẽ gửi message đến */
  @Bean
  public DirectExchange notificationExchange() {
    return new DirectExchange(EXCHANGE_NAME);
  }

  /** Queue: */
  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable(QUEUE_NAME)
      .withArgument("x-dead-letter-exchange", EXCHANGE_NAME) // Khi lỗi → gửi lại vào exchange
      .withArgument("x-dead-letter-routing-key", "notification.retry")
      .build();
  }

  /** Queue dành cho Retry/Dead-letter */
  @Bean
  public Queue notificationDLQ() {
    return QueueBuilder.durable(DLQ_NAME)
      .withArgument("x-message-ttl", 60000) // 60s retry lại
      .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
      .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
      .build();
  }

  /** Binding: Nối queue chính với exchange */
  @Bean
  public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(notificationQueue)
      .to(notificationExchange)
      .with(ROUTING_KEY);
  }

  /** Binding: Nối DLQ với exchange (dùng cho retry) */
  @Bean
  public Binding dlqBinding(Queue notificationDLQ, DirectExchange notificationExchange) {
    return BindingBuilder.bind(notificationDLQ)
      .to(notificationExchange)
      .with("notification.retry");
  }
}
