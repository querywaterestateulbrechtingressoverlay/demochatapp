package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
  void trimChatroomCacheOnOrphanedCache() {
    String chatroomId = "room1";
    when(chatKeyIndexTemplate.hasKey("chatroom:room1")).thenReturn(false);

    redisChatMessageCache.trimChatroomCache(chatroomId);

    verify(chatKeyIndexTemplate.opsForSet()).remove("cachedchatrooms", chatroomId);
    verify(chatMessageCacheTemplate.opsForList(), never()).trim(any(), anyLong(), anyLong());
  }

  @Test
  void trimChatroomCache() {
    String chatroomId = "room1";
    when(chatKeyIndexTemplate.hasKey("chatroom:room1")).thenReturn(true);

    redisChatMessageCache.trimChatroomCache(chatroomId);

    verify(chatMessageCacheTemplate.opsForList()).trim("chatroom:room1", 0, 10);
    verify(chatKeyIndexTemplate.opsForSet(), never()).remove(any(), any());
  }

  @Test
  void getMessagesFromCache() {
    List<ChatMessage> expectedMessages = List.of(
      new ChatMessage("1", "user1", "room1", Instant.now(), "Hello")
    );
    when(chatMessageCacheTemplate.opsForList().range("chatroom:room1", 0, 5))
      .thenReturn(expectedMessages);

    List<ChatMessage> result = redisChatMessageCache.getMessagesFromCache("room1", 5);

    assertEquals(expectedMessages, result);
  }

  @Test
  void isCachePresentOnOrphanedCache() {
    String chatroomId = "room1";
    when(chatKeyIndexTemplate.opsForSet().isMember("cachedchatrooms", chatroomId))
      .thenReturn(true);
    when(chatMessageCacheTemplate.opsForList().size("chatroom:room1")).thenReturn(0L);

    boolean result = redisChatMessageCache.isCachePresent(chatroomId);

    assertFalse(result);
    verify(chatKeyIndexTemplate.opsForSet()).remove("cachedchatrooms", chatroomId);
  }

  @Test
  void isCachePresentOnExistingCache() {
    String chatroomId = "room1";
    when(chatKeyIndexTemplate.opsForSet().isMember("cachedchatrooms", chatroomId))
      .thenReturn(true);
    when(chatMessageCacheTemplate.opsForList().size("chatroom:room1")).thenReturn(1L);

    boolean result = redisChatMessageCache.isCachePresent(chatroomId);

    assertTrue(result);
  }

  @Test
  void isChatroomPresentInCachedSet() {
    String chatroomId = "room1";
    when(chatKeyIndexTemplate.opsForSet().isMember("cachedchatrooms", chatroomId))
      .thenReturn(true);

    boolean result = redisChatMessageCache.isChatroomPresentInCachedSet(chatroomId);

    assertTrue(result);
    verify(chatKeyIndexTemplate.opsForSet()).isMember("cachedchatrooms", chatroomId);
  }

  @Test
  void isChatroomCachePresentCachePresent() {
    String chatroomId = "room1";
    when(chatMessageCacheTemplate.opsForList().size("chatroom:room1")).thenReturn(1L);

    boolean result = redisChatMessageCache.isChatroomCachePresent(chatroomId);

    assertTrue(result);
  }
  @Test
  void isChatroomCachePresentCacheMissing() {
    String chatroomId = "room1";
    when(chatMessageCacheTemplate.opsForList().size("chatroom:room1")).thenReturn(0L);

    boolean result = redisChatMessageCache.isChatroomCachePresent(chatroomId);

    assertFalse(result);
  }


  @Test
  void removeOrphanedCache() {
    redisChatMessageCache.removeOrphanedCache("room1");

    verify(chatKeyIndexTemplate.opsForSet()).remove("cachedchatrooms", "room1");
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