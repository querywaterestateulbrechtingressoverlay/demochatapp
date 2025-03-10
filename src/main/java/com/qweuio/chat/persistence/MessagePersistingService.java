package com.qweuio.chat.persistence;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.repository.MessageRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessagePersistingService {
  final String sendMsgTopic = "{chatapp.kafka.send-message-topic}";
  @Autowired
  MessageRepository messageRepository;

  @KafkaListener(topics = sendMsgTopic)
  public void persistMessage(ProcessedMessageDTO message) {
    messageRepository.save(new ChatMessage(null, message.senderId(), message.recipientId(), Instant.now(), message.message()));
  }
}
