package com.qweuio.chat.messaging;

import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
  @Value("{chatapp.kafka.message-topic}")
  String sendMsgTopic;
  KafkaTemplate<String, Object> kafkaTemplate;
  public void sendMessage(ProcessedMessageDTO message) {
    kafkaTemplate.send(sendMsgTopic, message);
  }
}
