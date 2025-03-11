package com.qweuio.chat.messaging;

import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
  @Value("${chatapp.kafka.message-topic}")
  String sendMsgTopic;
  @Autowired
  KafkaTemplate<String, ProcessedMessageDTO> kafkaMessageTemplate;
  public void sendMessage(ProcessedMessageDTO message) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }
//  public void
}
