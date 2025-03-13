package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class MessagePersistingService {
  private final int RECENT_MESSAGE_COUNT = 10;
  private final Logger logger = LoggerFactory.getLogger(MessagePersistingService.class);
  final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  @Autowired
  ChatMessageRepository messageRepository;
  @Autowired
  RedisTemplate<String, ProcessedMessageDTO> chatMessageCacheTemplate;

  @KafkaListener(id = "messagePersister", topics = sendMsgTopic)
  public void persistMessage(ProcessedMessageDTO message) {
    logger.info("persisting message sent from {} to chatroom {} at {}", message.senderId(), message.chatroomId(), Instant.now());
    chatMessageCacheTemplate.opsForList().leftPush("chatroom-" + message.chatroomId(), message);
    messageRepository.save(new ChatMessage(null, message.senderId(), message.chatroomId(), Instant.now(), message.message()));
  }
//  public List<ProcessedMessageDTO> getRecentMessages(String chatroomId) {
//    if (chatMessageCacheTemplate.hasKey(chatroomId)) {
//      if (chatMessageCacheTemplate.opsForList().size(chatroomId) >= RECENT_MESSAGE_COUNT) {
//        return chatMessageCacheTemplate.opsForList().range(chatroomId, 0, RECENT_MESSAGE_COUNT);
//      }
//    }
//  }
}
