package com.qweuio.chat.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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
//    GenericJackson2JsonRedisSerializer serializer = GenericJackson2JsonRedisSerializer.builder()
//        .objectMapper(
//          JsonMapper.builder()
//            .addModule(new JavaTimeModule())
//            .findAndAddModules()
//            .build()
//        ).build();
    //    template.setValueSerializer(serializer);
    template.setKeySerializer(new StringRedisSerializer());
    var valueSerializer = new Jackson2JsonRedisSerializer<ProcessedMessageDTO>(JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .findAndAddModules()
            .build(), ProcessedMessageDTO.class);
    template.setValueSerializer(valueSerializer);
    return template;
  }
}
