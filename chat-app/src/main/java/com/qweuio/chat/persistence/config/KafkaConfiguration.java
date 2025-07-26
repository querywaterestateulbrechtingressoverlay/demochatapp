package com.qweuio.chat.persistence.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {
  @Value("${chatapp.kafka.host}")
  String kafkaHost;
  @Value("${chatapp.kafka.update-topic}")
  String updateTopic;
  @Bean
  public KafkaAdmin admin() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
    return new KafkaAdmin(properties);
  }
  @Bean
  public ProducerFactory<Object, Object> producerFactoryConfig() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);

    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(properties);
  }

  @Bean
  public ConsumerFactory<Object, Object> consumerFactoryConfig() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
    return new DefaultKafkaConsumerFactory<>(properties, new JsonDeserializer<>(), new JsonDeserializer<>()
      .trustedPackages("*"));
  }

  @Bean
  public NewTopic updateTopic() {
    return TopicBuilder.name(updateTopic)
      .partitions(4)
      .build();
  }
}
