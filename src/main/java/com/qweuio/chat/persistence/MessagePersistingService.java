package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
  RedisTemplate<String, ProcessedMessageDTO> chatMessageCacheTemplate;

  @Autowired
  RedisTemplate<String, String> chatKeyIndexTemplate;

  @Scheduled(fixedRate = 600000)
  public void trimCache() {
    try (Cursor<String> cursor = chatKeyIndexTemplate.opsForSet().scan(CACHED_CHATROOMS, ScanOptions.NONE)) {
      cursor.forEachRemaining((chatroomId) -> {
        if (!chatKeyIndexTemplate.hasKey(chatroomId)) {
          chatKeyIndexTemplate.opsForSet().remove(CACHED_CHATROOMS, chatroomId);
        } else {
          chatMessageCacheTemplate.opsForList().trim(CHATROOM_PREFIX + chatroomId, 0, 10);
        }
      });
    }
  }
  @KafkaListener(id = "messagePersister", topics = sendMsgTopic)
  public void persistMessage(ProcessedMessageDTO message) {
    logger.info("persisting message sent from {} to chatroom {} at {}", message.senderId(), message.chatroomId(), Instant.now());
    if (chatMessageCacheTemplate.opsForList().leftPush(CHATROOM_PREFIX + message.chatroomId(), message) == 1) {
      chatKeyIndexTemplate.opsForSet().add(CACHED_CHATROOMS, message.chatroomId());
    }
    chatMessageCacheTemplate.expire(CHATROOM_PREFIX + message.chatroomId(), Duration.ofHours(3));
    messageRepository.save(new ChatMessage(null, message.senderId(), message.chatroomId(), Instant.now(), message.message()));
  }
//  public List<ProcessedMessageDTO> getRecentMessages(String chatroomId) {
//    if (chatMessageCacheTemplate.hasKey(chatroomId)) {
//      if (chatMessageCacheTemplate.opsForList().size(chatroomId) > 0) {
//        return chatMessageCacheTemplate.opsForList().range(chatroomId, 0, RECENT_MESSAGE_COUNT);
//      }
//    }
//
//  }
}
