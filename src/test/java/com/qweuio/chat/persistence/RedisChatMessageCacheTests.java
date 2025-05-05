package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = {RedisChatMessageCache.class})
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisChatMessageCacheTests {
  @MockitoBean
  RedisTemplate<String, ChatMessage> chatMessageCacheTemplate;
  @MockitoBean
  RedisTemplate<String, String> chatKeyIndexTemplate;
  @MockitoBean
  ListOperations<String, ChatMessage> listOperations;
  @MockitoBean
  SetOperations<String, String> setOperations;
  @Autowired
  RedisChatMessageCache redisChatMessageCache;

  @BeforeEach
  public void setUp() {
    when(chatMessageCacheTemplate.opsForList()).thenReturn(listOperations);
    when(chatKeyIndexTemplate.opsForSet()).thenReturn(setOperations);
  }


  @Test
  void trimCache() {
  }

  @Test
  void trimChatroomCache() {
  }

  @Test
  void getMessagesFromCache() {
  }

  @Test
  void isCachePresent() {
  }

  @Test
  void isChatroomPresentInCachedSet() {
  }

  @Test
  void isChatroomCachePresent() {

  }

  @Test
  void removeOrphanedCache() {
    redisChatMessageCache.removeOrphanedCache("room1");

    verify(chatKeyIndexTemplate.opsForSet()).remove("cachedchatrooms", "chatroom:room1");
  }

  @Test
  public void cacheMessageWithoutExistingCache() throws Exception {
    ChatMessage message = new ChatMessage("1", "user1", "room1", Instant.now(), "yo");
    when(chatMessageCacheTemplate.opsForList().leftPush(any(), any())).thenReturn(1L);

    redisChatMessageCache.cacheMessage(message);

    verify(chatMessageCacheTemplate.opsForList()).leftPush("chatroom:room1", message);
    verify(chatKeyIndexTemplate.opsForSet()).add("cachedchatrooms", "room1");
    verify(chatMessageCacheTemplate).expire("chatroom:room1", Duration.ofMinutes(5));
  }

  @Test
  public void cacheMessageWithExistingCache() throws Exception {
    ChatMessage message = new ChatMessage("1", "user1", "room1", Instant.now(), "yo");
    when(chatMessageCacheTemplate.opsForList().leftPush(any(), any())).thenReturn(2L);

    redisChatMessageCache.cacheMessage(message);

    verify(chatMessageCacheTemplate.opsForList()).leftPush("chatroom:room1", message);
    verify(chatKeyIndexTemplate.opsForSet(), never()).add("cachedchatrooms", "room1");
    verify(chatMessageCacheTemplate).expire("chatroom:room1", Duration.ofMinutes(5));
  }
}