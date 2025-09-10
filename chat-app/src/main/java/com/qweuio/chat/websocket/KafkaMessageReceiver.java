package com.qweuio.chat.websocket;

import com.qweuio.chat.websocket.dto.outbound.ChatroomListDTO;
import com.qweuio.chat.websocket.dto.messaging.ChatroomShortInfoDTO;
import com.qweuio.chat.websocket.dto.messaging.ChatroomListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.ErrorResponseDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import com.qweuio.chat.websocket.dto.messaging.UserListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.UserListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class KafkaMessageReceiver {
  Logger logger = LoggerFactory.getLogger(KafkaMessageReceiver.class);
  String messageDest = "/messages";
  String messageHistoryDest = "/chatrooms/messages";
  String chatroomListUpdateDest = "/chatrooms";
  String chatroomUserListUpdDest = "/userlist";
  String errorDest = "/errors";

  @Autowired
  SimpMessagingTemplate template;

  @Autowired
  WebSocketUserManagerService userManagerService;

  @KafkaListener(id = "errorHandler", topicPartitions = {
    @TopicPartition(topic = "${chatapp.kafka.update-topic}", partitions = "${chatapp.kafka.update-topic-partition.error}") })
  void receiveErrorMessage(ErrorResponseDTO errorResponseDTO) {
    template.convertAndSendToUser(errorResponseDTO.recipientId().toString(), errorDest, errorResponseDTO);
  }

  @KafkaListener(id = "messageHandler", topicPartitions = {
    @TopicPartition(topic = "${chatapp.kafka.update-topic}", partitions = "${chatapp.kafka.update-topic-partition.message}")
  })
  void receiveMessage(MessageDTO message) {
    logger.info("received message {}", message.toString());
    var messageRecipients = userManagerService.getChatroomConnectedClients(message.chatroom());
    logger.info("recipients: {}", messageRecipients.toString());
    for (UUID recipientId : messageRecipients) {
      template.convertAndSendToUser(recipientId.toString(), messageDest, message);
    }
  }

  @KafkaListener(id = "chatroomListUpdateHandler", topicPartitions = {
    @TopicPartition(topic = "${chatapp.kafka.update-topic}", partitions = "${chatapp.kafka.update-topic-partition.chatroom-list}")
  })
  public void receiveChatroomListUpdate(ChatroomListUpdateDTO chatroomListUpdate) {
    logger.info("received a chatroom update {}", chatroomListUpdate.toString());
    if (userManagerService.isUserConnected(chatroomListUpdate.recipientId())) {
      logger.info("user {} is connected", chatroomListUpdate.recipientId());
      template.convertAndSendToUser(chatroomListUpdate.recipientId().toString(), chatroomListUpdateDest, new ChatroomListDTO(chatroomListUpdate.chatrooms()));
      for (ChatroomShortInfoDTO chatroom : chatroomListUpdate.chatrooms()) {
        if (chatroomListUpdate.operation() == ChatroomListUpdateDTO.Operation.ADD) {
          userManagerService.addUserToChatroom(chatroom.id(), chatroomListUpdate.recipientId());
        } else {
          userManagerService.removeUserFromChatroom(chatroom.id(), chatroomListUpdate.recipientId());
        }
      }
    } else {
      logger.info("user {} is not connected", chatroomListUpdate.recipientId());
    }
  }

  @KafkaListener(id = "chatroomUserListUpdateHandler", topicPartitions = {
    @TopicPartition(topic = "${chatapp.kafka.update-topic}", partitions = "${chatapp.kafka.update-topic-partition.chatroom-user-list}")
  })
  void receiveUserListUpdate(UserListUpdateDTO userListUpdate) {
    logger.info("received a user list update {}", userListUpdate.toString());
    if (userManagerService.isChatroomPresent(userListUpdate.chatroomId())) {
      logger.info("connected users: {}", userManagerService.getChatroomConnectedClients(userListUpdate.chatroomId()));
      for (UUID userId : userManagerService.getChatroomConnectedClients(userListUpdate.chatroomId())) {
        logger.info("sending message to user {}", userId);
        template.convertAndSendToUser(
            userId.toString(),
            chatroomUserListUpdDest,
            new UserListDTO(userListUpdate.chatroomId(), userListUpdate.userListUpdate()),
            Map.of("operation", userListUpdate.operation().name().toLowerCase()));
      }
    }
  }
}
