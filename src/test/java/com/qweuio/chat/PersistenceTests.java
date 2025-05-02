package com.qweuio.chat;

import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

@Testcontainers
@SpringBootTest(classes = {MessagePersistingService.class, ChatMessageRepository.class})
public class PersistenceTests {
  @Container
  private static RedisContainer container = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));
  @Autowired
  MessagePersistingService msgPersistingService;
  @Autowired
  ChatMessageRepository chatMsgRepo;
  @BeforeEach
  void deleteAll() {
    try (StatefulRedisConnection<String, String> conn = RedisClient.create(container.getRedisURI()).connect()) {
      conn.sync().del(conn.sync().keys("*").toArray(String[]::new));
    }
  }
  @Test
  public void asd() {
    try (StatefulRedisConnection<String, String> conn = RedisClient.create(container.getRedisURI()).connect()) {
      msgPersistingService.cacheMessage(new ChatMessage("a", "b", "c", Instant.now(), "d"));
      org.junit.jupiter.api.Assertions.assertEquals(conn.sync().keys("chatroom:a").size(), 1);
    }
  }
}
