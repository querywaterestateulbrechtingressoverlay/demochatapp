package com.qweuio.chat.persistence.config;

import tools.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qweuio.chat.persistence.entity.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson3JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
//  @Value("${chatapp.redis.host}")
//  private String redisHost;
//  @Value("${chatapp.redis.password}")
//  private String redisPassword;
//
//  @Bean
//  public RedisConnectionFactory connFactory() {
//    var redisConfig = new RedisStandaloneConfiguration();
//    redisConfig.setHostName(redisHost);
//    redisConfig.setUsername(redisUsername);
//    redisConfig.setPassword(redisPassword);
//    return new LettuceConnectionFactory(redisConfig);
//  }
  @Bean
  public RedisTemplate<String, Message> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Message> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    var valueSerializer = new Jackson3JsonRedisSerializer<>(JsonMapper.builder()
//            .addModule(new JavaTimeModule())
            .findAndAddModules()
            .build(), Message.class);
    template.setValueSerializer(valueSerializer);
    return template;
  }
}
