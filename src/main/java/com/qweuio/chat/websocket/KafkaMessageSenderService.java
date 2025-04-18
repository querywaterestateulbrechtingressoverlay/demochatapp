package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.websocket.dto.ChatUserListDTO;
import com.qweuio.chat.websocket.dto.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.UserShortInfoDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMessageSenderService implements MessageSenderService {
  @Value("${chatapp.kafka.message-topic}")
  private String sendMsgTopic;
  @Value("${chatapp.kafka.user-list-topic}")
  private String userListTopic;
  @Autowired
  KafkaTemplate<String, Object> kafkaMessageTemplate;
  public void sendMessage(MessageDTO message) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }

  public void updateUserList(ChatUserListDTO userList) {
    kafkaMessageTemplate.send(userListTopic, userList);
  }
  @Override
  public void sendMessage(ChatMessage message, String chatroomId) {

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
