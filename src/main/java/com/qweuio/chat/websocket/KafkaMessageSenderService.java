package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.websocket.dto.ChatUserListDTO;
import com.qweuio.chat.websocket.dto.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.UserShortInfoDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMessageSenderService implements MessageSenderService {
  private final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  private final String userListTopic = "${chatapp.kafka.user-list-topic}";

  @Autowired
  KafkaTemplate<String, Object> kafkaMessageTemplate;

  @Autowired
  SimpMessagingTemplate template;

  public void sendMessage(MessageDTO message) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }

  public void updateUserList(ChatUserListDTO userList) {
    kafkaMessageTemplate.send(userListTopic, userList);
  }

  @KafkaListener(topics = {sendMsgTopic})
  void receiveMessage(MessageDTO message) {
    var messageRecipients = 
    template.convertAndSendToUser(recipientId, messageDest, message);
  }

  @Override
  public void sendMessage(ChatMessage message, String chatroomId) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }

  @Override
  public void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history) {

  }

  @Override
  public void addChatroomToUser(String userId, ChatroomListDTO list) {

  }

  @Override
  public void removeChatroomFromUser(String userId, String chatroomId) {

  }

  @Override
  public void addUserToChatroom(String chatroomId, UserShortInfoDTO user) {

  }

  @Override
  public void addUsersToChatroom(String chatroomId, List<UserShortInfoDTO> users) {

  }

  @Override
  public void removeUserFromChatroom(String chatroomId, String userId) {

  }
}
