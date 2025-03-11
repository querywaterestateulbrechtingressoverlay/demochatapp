package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessagePersistingService {
  private final Logger logger = LoggerFactory.getLogger(MessagePersistingService.class);
  final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  @Autowired
  ChatMessageRepository messageRepository;

  @KafkaListener(id = "messagePersister", topics = sendMsgTopic)
  public void persistMessage(ProcessedMessageDTO message) {
    logger.info("persisting message sent from {} to chatroom {} at {}", message.senderId(), message.chatroomId(), Instant.now());
    messageRepository.save(new ChatMessage(null, message.senderId(), message.chatroomId(), Instant.now(), message.message()));
  }
}
