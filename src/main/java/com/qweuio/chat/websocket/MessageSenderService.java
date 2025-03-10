package com.qweuio.chat.websocket;

import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageSenderService {
  final String sendMsgTopic = "{chatapp.kafka.send-message-topic}";
  @Autowired
  SimpMessagingTemplate template;
  @KafkaListener(topics = sendMsgTopic)
  public void send(ProcessedMessageDTO message) {
    template.convertAndSendToUser(message.recipientId().toString(), "/messages", message);
  }
}
