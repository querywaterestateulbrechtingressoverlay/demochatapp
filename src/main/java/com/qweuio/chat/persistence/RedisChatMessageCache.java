package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisChatMessageCache {
  private final int RECENT_MESSAGE_COUNT = 10;

  private final String CACHED_CHATROOMS = "cachedchatrooms";
  private final String CHATROOM_PREFIX = "chatroom:";
  private final long CACHE_TRIM_INTERVAL = 600_000L;
  private final Duration CACHE_LIFESPAN =  Duration.ofMinutes(5);

  @Autowired
  RedisTemplate<String, ChatMessage> chatMessageCacheTemplate;

  @Autowired
  RedisTemplate<String, String> chatKeyIndexTemplate;

  @Scheduled(fixedRate = CACHE_TRIM_INTERVAL)
  public void trimCache() {
    try (Cursor<String> cursor = chatKeyIndexTemplate.opsForSet().scan(CACHED_CHATROOMS, ScanOptions.NONE)) {
      cursor.forEachRemaining(this::trimChatroomCache);
    }
  }

  public void trimChatroomCache(String chatroomId) {
    if (!chatKeyIndexTemplate.hasKey(CHATROOM_PREFIX + chatroomId)) {
      chatKeyIndexTemplate.opsForSet().remove(CACHED_CHATROOMS, chatroomId);
    } else {
      chatMessageCacheTemplate.opsForList().trim(CHATROOM_PREFIX + chatroomId, 0, RECENT_MESSAGE_COUNT);
    }
  }

  public List<ChatMessage> getMessagesFromCache(String chatroomId, int messageCount) {
    return chatMessageCacheTemplate.opsForList().range(CHATROOM_PREFIX + chatroomId, 0, messageCount);
  }

  public boolean isCachePresent(String chatroomId) {
    if (isChatroomPresentInCachedSet(chatroomId)) {
      if (isChatroomCachePresent(chatroomId)) {
        return true;
      }
      removeOrphanedCache(chatroomId);
    }
    return false;
  }

  public boolean isChatroomPresentInCachedSet(String chatroomId) {
    return chatKeyIndexTemplate.opsForSet().isMember(CACHED_CHATROOMS, chatroomId);
  }

  public boolean isChatroomCachePresent(String chatroomId) {
    return chatMessageCacheTemplate.opsForList().size(CHATROOM_PREFIX + chatroomId) > 0;
  }

  public void removeOrphanedCache(String chatroomId) {
    chatKeyIndexTemplate.opsForSet().remove(CACHED_CHATROOMS, CHATROOM_PREFIX + chatroomId);
  }

  public void cacheMessage(ChatMessage message) {
    if (chatMessageCacheTemplate.opsForList().leftPush(CHATROOM_PREFIX + message.chatroomId(), message) == 1) {
      chatKeyIndexTemplate.opsForSet().add(CACHED_CHATROOMS, message.chatroomId());
    }
    chatMessageCacheTemplate.expire(CHATROOM_PREFIX + message.chatroomId(), CACHE_LIFESPAN);
  }
}
