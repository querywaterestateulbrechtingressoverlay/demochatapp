package com.qweuio.chat.messaging;

import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.ParseStringDeserializer;
import org.springframework.kafka.support.serializer.ToStringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {
  @Value("${chatapp.kafka.host}")
  String kafkaHost;
  @Value("${chatapp.kafka.message-topic}")
  String sendMsgTopic;
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
  public NewTopic topic1() {
    return TopicBuilder.name(sendMsgTopic)
      .build();
  }
}
