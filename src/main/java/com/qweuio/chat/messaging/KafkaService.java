package com.qweuio.chat.messaging;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.websocket.dto.ChatUserListDTO;
import com.qweuio.chat.websocket.dto.ProcessedMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
  @Value("${chatapp.kafka.message-topic}")
  String sendMsgTopic;
  @Value("${chatapp.kafka.user-list-topic}")
  String userListTopic;
  @Autowired
  KafkaTemplate<String, Object> kafkaMessageTemplate;
  public void sendMessage(ProcessedMessageDTO message) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }

  public void updateUserList(ChatUserListDTO userList) {
    kafkaMessageTemplate.send(userListTopic, userList);
  }
}
