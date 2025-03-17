package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.*;
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
  final String userListTopic = "${chatapp.kafka.user-list-topic}";
  @Autowired
  SimpMessagingTemplate template;
  @Autowired
  ChatroomRepository chatroomRepository;
  @KafkaListener(id = "messageSender", topics = sendMsgTopic)
  public void sendMessage(ProcessedMessageDTO message) {
    logger.trace("sending message to chatroom {}", message.chatroomId());
    for (UserWithRoleEntity user : chatroomRepository.findById(message.chatroomId()).get().users()) {
      template.convertAndSendToUser(user.userId(), "/messages", message);
    }
  }
  @KafkaListener(id = "userListUpdater", topics = userListTopic)
  public void updateUserList(ChatUserListDTO userList) {
    logger.debug("broadcasting an updated list of users to members of chatroom {}", userList.chatId());
    for (UserShortInfoDTO user : userList.users()) {
      logger.trace("sending an updated list of users of chatroom {} to user {}", userList.chatId(), user.id());
      template.convertAndSendToUser(user.id(), "/userlist", userList);
    }
  }
  public void updateChatroomList(ChatroomListDTO chatrooms, String userId) {

  }
  public void updateChatHistory(ChatHistoryResponseDTO chatHistory, String userId) {

  }
}
