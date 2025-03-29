package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageSenderService {
  Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
  final String sendMsgTopic = "${chatapp.kafka.message-topic}";
  final String userListTopic = "${chatapp.kafka.user-list-topic}";
  @Autowired
  SimpMessagingTemplate template;
  @Autowired
  ChatroomRepository chatroomRepository;

  public void sendMessage(ProcessedMessageDTO message) {
    logger.trace("sending message to chatroom {}", message.chatroomId());
    for (UserWithRoleEntity user : chatroomRepository.findById(message.chatroomId()).get().users()) {
      template.convertAndSendToUser(user.userId(), "/messages", message);
    }
  }

  public void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history) {
    template.convertAndSendToUser(userId, "/", new ChatHistoryResponseDTO(chatroomId, ));
  }

  public void addChatroomToUser(String userId, ChatroomListDTO list) {
    template.convertAndSendToUser(userId, "/chatroom", list, Map.of("OPERATION", "ADD"));
  }

  public void removeChatroomFromUser(String userId, String chatroomId) {
    template.convertAndSendToUser(userId, "/chatroom", chatroomId, Map.of("OPERATION", "REMOVE"));
  }

  public void updateUserListForChatroom(ChatUserListDTO userList) {
    logger.debug("broadcasting an updated list of users to members of chatroom {}", userList.chatId());
    for (UserShortInfoDTO user : userList.users()) {
      logger.trace("sending an updated list of users of chatroom {} to user {}", userList.chatId(), user.id());
      template.convertAndSendToUser(user.id(), "/userlist", userList);
    }
  }
}
