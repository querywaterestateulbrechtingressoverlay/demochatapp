package com.qweuio.chat.websocket;

import com.qweuio.chat.websocket.dto.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.ChatroomShortInfoDTO;
import com.qweuio.chat.websocket.dto.outbound.ChatroomListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.ErrorDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import com.qweuio.chat.websocket.dto.outbound.UserListUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaUpdateReceiver {
  Logger logger = LoggerFactory.getLogger(KafkaUpdateReceiver.class);
  String messageDest = "/messages";
  String messageHistoryDest = "/chatrooms/messages";
  String chatroomListUpdateDest = "/chatrooms";
  String chatroomUserListUpdDest = "/userlist";
  String errorDest = "/errors";

  @Autowired
  SimpMessagingTemplate template;

  @Autowired
  WebSocketUserManagerService userManagerService;

  @KafkaListener(id = "errorSender", topics = {"${chatapp.kafka.error-topic}"})
  void receiveErrorMEssage(ErrorDTO errorDTO) {
    template.convertAndSendToUser(errorDTO.recipientId(), errorDest, errorDTO);
  }

  @KafkaListener(id = "messageSender", topics = {"${chatapp.kafka.message-topic}"})
  void receiveMessage(MessageDTO message) {
    logger.info("received message " + message.toString());
    var messageRecipients = userManagerService.getChatroomConnectedClients(message.chatroom());
    logger.info("recipients: " + messageRecipients.toString());
    for (String recipientId : messageRecipients) {
      template.convertAndSendToUser(recipientId, messageDest, message);
    }
  }

  @KafkaListener(id = "chatroomListUpdater", topics = {"${chatapp.kafka.chatroom-list-update-topic}"})
  public void receiveChatroomListUpdate(ChatroomListUpdateDTO chatroomListUpdate) {
    logger.info("received a chatroom update " + chatroomListUpdate.toString());
    if (userManagerService.isUserConnected(chatroomListUpdate.recipientId())) {
      logger.info("user " + chatroomListUpdate.recipientId() + " is connected");
      template.convertAndSendToUser(chatroomListUpdate.recipientId(), chatroomListUpdateDest, new ChatroomListDTO(chatroomListUpdate.chatrooms()));
      for (ChatroomShortInfoDTO chatroom : chatroomListUpdate.chatrooms()) {
        if (chatroomListUpdate.operation() == ChatroomListUpdateDTO.Operation.ADD) {
          userManagerService.addUserToChatroom(chatroom.id(), chatroomListUpdate.recipientId());
        } else {
          userManagerService.removeUserFromChatroom(chatroom.id(), chatroomListUpdate.recipientId());
        }
      }
    } else {
      logger.info("user " + chatroomListUpdate.recipientId() + " is not connected");
    }
  }

  @KafkaListener(id = "userListUpdater", topics = {"${chatapp.kafka.user-list-update-topic}"})
  void receiveUserListUpdate(UserListUpdateDTO userListUpdate) {
    logger.info("received a user list update " + userListUpdate.toString());
    if (userManagerService.isChatroomPresent(userListUpdate.chatroomId())) {
      logger.info("connected users: " + userManagerService.getChatroomConnectedClients(userListUpdate.chatroomId()));
      for (String userId : userManagerService.getChatroomConnectedClients(userListUpdate.chatroomId())) {
        logger.info("sending message to user " + userId);
        template.convertAndSendToUser(userId, chatroomUserListUpdDest, userListUpdate.chatroomId(), Map.of("operation", userListUpdate.operation().name().toLowerCase()));
      }
    }
  }
}
