//package com.qweuio.chat;
//
//import com.qweuio.chat.persistence.MessagePersistingService;
//import com.qweuio.chat.persistence.RedisChatMessageCache;
//import com.qweuio.chat.persistence.entity.ChatMessage;
//import com.qweuio.chat.persistence.repository.ChatMessageRepository;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.testcontainers.containers.MongoDBContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.time.Instant;
//
//import static org.mockito.Mockito.*;
//
//@Testcontainers
//@DataMongoTest
//@Import({MessagePersistingService.class})
//@TestPropertySource(locations="classpath:test.properties")
//public class PersistenceTests {
////  @Container
////  private static RedisContainer redisContainer = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));
//  @Container
//  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest").withExposedPorts(27017);
//
//  @MockitoBean
//  RedisChatMessageCache redisChatMessageCache;
//  @Autowired
//  RedisTemplate<String, ChatMessage> chatMessageCacheTemplate;
//
//  @Autowired
//  MessagePersistingService msgPersistingService;
//  @Autowired
//  ChatMessageRepository chatMsgRepo;
//
//  @Autowired
//  MongoTemplate mongoTemplate;
//
//  @DynamicPropertySource
//  static void containersProperties(DynamicPropertyRegistry registry) {
//    mongoDBContainer.start();
//    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
//    registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
//  }
//
//  @AfterAll
//  public static void stopContainer() {
////    redisContainer.stop();
//    mongoDBContainer.stop();
//  }
//  @BeforeEach
//  void deleteAll() {
////    try (StatefulRedisConnection<String, String> conn = RedisClient.create(redisContainer.getRedisURI()).connect()) {
////      conn.sync().del(conn.sync().keys("*").toArray(String[]::new));
////    }
//  }
////  @Test
////  public void cacheMessage_shouldAddToRedisListAndSetWithTTL() {
////    // Arrange
////    ChatMessage message = new ChatMessage("1", "user1", "room1", Instant.now(), "Hi");
////    when(chatMessageCacheTemplate.opsForList().leftPush(any(), any())).thenReturn(1L);
////
////    // Act
////    redisChatMessageCache.cacheMessage(message);
////
////    // Assert
////    verify(chatMessageCacheTemplate.opsForList()).leftPush("chatroom:room1", message);
////    verify(chatKeyIndexTemplate.opsForSet()).add("cachedchatrooms", "room1");
////    verify(chatMessageCacheTemplate).expire("chatroom:room1", Duration.ofMinutes(5));
//  }
//}
