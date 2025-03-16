package com.qweuio.chat.persistence;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {
  @Value("${chatapp.redis.host}")
  private String redisHost;
  @Value("${chatapp.redis.username}")
  private String redisUsername;
  @Value("${chatapp.redis.password}")
  private String redisPassword;

  @Bean
  public RedisConnectionFactory connFactory() {
    var redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(redisHost);
    redisConfig.setUsername(redisUsername);
    redisConfig.setPassword(redisPassword);
    return new LettuceConnectionFactory(redisConfig);
  }
  @Bean
  public RedisTemplate<String, ProcessedMessageDTO> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, ProcessedMessageDTO> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setDefaultSerializer(RedisSerializer.json());
    return template;
  }
}
