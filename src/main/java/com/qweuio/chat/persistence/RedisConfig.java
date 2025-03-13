package com.qweuio.chat.persistence;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {
  @Bean
  public RedisTemplate<String, ProcessedMessageDTO> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, ProcessedMessageDTO> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setDefaultSerializer(RedisSerializer.json());
    return template;
  }
}
