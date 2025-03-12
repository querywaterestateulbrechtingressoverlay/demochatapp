package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.UserRole;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageSenderService {
  Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
  final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  @Autowired
  SimpMessagingTemplate template;
  @Autowired
  ChatroomRepository chatroomRepository;
  @KafkaListener(id = "messageSender", topics = sendMsgTopic)
  public void send(ProcessedMessageDTO message) {
    logger.info(message.toString());
    for (UserRole user : chatroomRepository.findById(message.chatroomId()).get().users()) {
      logger.info("broadcasting message from chatroom {} to user {}", message.chatroomId(), user.userId());
      template.convertAndSendToUser(user.userId(), "/messages", message);
    }
  }
}
