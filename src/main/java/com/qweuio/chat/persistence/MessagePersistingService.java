package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@EnableScheduling
@Service
public class MessagePersistingService {
  private final int RECENT_MESSAGE_COUNT = 10;
  private final String CACHED_CHATROOMS = "cached-chatrooms";
  private final String CHATROOM_PREFIX = "chatroom:";
  private final Logger logger = LoggerFactory.getLogger(MessagePersistingService.class);
  final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  @Autowired
  ChatMessageRepository messageRepository;
  @Autowired
  RedisTemplate<String, ChatMessage> chatMessageCacheTemplate;

  @Autowired
  RedisTemplate<String, String> chatKeyIndexTemplate;

  @Scheduled(fixedRate = 600000)
  public void trimCache() {
    try (Cursor<String> cursor = chatKeyIndexTemplate.opsForSet().scan(CACHED_CHATROOMS, ScanOptions.NONE)) {
      cursor.forEachRemaining((chatroomId) -> {
        if (!chatKeyIndexTemplate.hasKey(CHATROOM_PREFIX + chatroomId)) {
          chatKeyIndexTemplate.opsForSet().remove(CACHED_CHATROOMS, chatroomId);
        } else {
          chatMessageCacheTemplate.opsForList().trim(CHATROOM_PREFIX + chatroomId, 0, 10);
        }
      });
    }
  }

  public void cacheMessage(ChatMessage message) {
    if (chatMessageCacheTemplate.opsForList().leftPush(CHATROOM_PREFIX + message.chatroomId(), message) == 1) {
      chatKeyIndexTemplate.opsForSet().add(CACHED_CHATROOMS, message.chatroomId());
    }
    chatMessageCacheTemplate.expire(CHATROOM_PREFIX + message.chatroomId(), Duration.ofMinutes(5));
  }

  @KafkaListener(id = "messagePersister", topics = sendMsgTopic)
  public ChatMessage persistMessage(MessageDTO message) {
    ChatMessage messageEntity = messageRepository.save(new ChatMessage(null, message.sender(), message.chatroom(), message.timestamp(), message.content()));
    cacheMessage(messageEntity);
    return messageEntity;
  }

  public boolean isCachePresent(String chatroomId) {
    if (chatKeyIndexTemplate.opsForSet().isMember(CACHED_CHATROOMS, chatroomId)) {
      if (chatMessageCacheTemplate.opsForList().size(CHATROOM_PREFIX + chatroomId) > 0) {
        return true;
      } else {
        chatKeyIndexTemplate.opsForSet().remove(CACHED_CHATROOMS, CHATROOM_PREFIX + chatroomId);
      }
    }
    return false;
  }

  public List<ChatMessage> getRecentMessages(String chatroomId) {
    if (isCachePresent(chatroomId)) {
      return chatMessageCacheTemplate.opsForList().range(CHATROOM_PREFIX + chatroomId, 0, RECENT_MESSAGE_COUNT);
    } else {
      List<ChatMessage> persistedMessages = messageRepository.findTop10ByChatroomId(chatroomId)
        .stream()
        .toList();
      persistedMessages.forEach(this::cacheMessage);
      return persistedMessages;
    }
  }
}
