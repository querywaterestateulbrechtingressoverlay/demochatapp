package com.qweuio.chat.messaging;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {
  @Value("{chatapp.kafka.host}");
  String kafkaHost;
  @Value("{chatapp.kafka.message-topic}")
  String sendMsgTopic;
  @Bean
  public KafkaAdmin admin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic topic1() {
    return TopicBuilder.name(sendMsgTopic)
      .build();
  }
}
