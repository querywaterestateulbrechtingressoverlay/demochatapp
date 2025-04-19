package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.websocket.dto.outbound.ChatroomListUpdateDTO;
import com.qweuio.chat.websocket.dto.outbound.MessageDTO;
import com.qweuio.chat.websocket.dto.outbound.UserListUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.qweuio.chat.websocket.dto.outbound.UserListUpdateDTO.Operation.ADD;

@Service
public class KafkaMessageSenderService implements MessageSenderService {
  @Value("${chatapp.kafka.message-topic}")
  private String sendMsgTopic;
  @Value("${chatapp.kafka.user-list-update-topic}")
  private String userListTopic;
  @Value("${chatapp.kafka.chatroom-list-update-topic}")
  private String chatroomListTopic;

  @Autowired
  KafkaTemplate<String, Object> kafkaMessageTemplate;

  @Autowired
  SimpMessagingTemplate template;

  @Autowired
  ChatroomRepository chatroomRepo;

  @Autowired
  WebSocketUserManagerService userManagerService;

  public void sendMessage(MessageDTO message) {
    kafkaMessageTemplate.send(sendMsgTopic, message);
  }

  public void updateUserList(ChatUserListDTO userList) {
    kafkaMessageTemplate.send(userListTopic, userList);
  }

  @KafkaListener(id = "messageSender", topics = {"${chatapp.kafka.message-topic}"})
  void receiveMessage(MessageDTO message) {
    var messageRecipients = userManagerService.getChatroomConnectedClients(message.chatroom());
    for (String recipientId : messageRecipients) {
      template.convertAndSendToUser(recipientId, messageDest, message);
    }
  }

  @Override
  public void sendMessage(ChatMessage message, String chatroomId) {
    kafkaMessageTemplate.send(sendMsgTopic, new MessageDTO(message.id(), message.senderId(), message.chatroomId(), message.sentAt(), message.contents()));
  }

  @Override
  public void updateMessageHistory(String userId, String chatroomId, List<ChatMessage> history) {
    template.convertAndSendToUser(userId, messageHistoryDest, new ChatHistoryResponseDTO(chatroomId, history.stream().map(Converters::toDTO).toList()));
  }


  @KafkaListener(id = "chatroomListUpdater", topics = {"${chatapp.kafka.chatroom-list-update-topic}"})
  public void receiveChatroomListUpdate(ChatroomListUpdateDTO chatroomListUpdate) {
    if (userManagerService.isUserConnected(chatroomListUpdate.recipientId())) {
      template.convertAndSendToUser(chatroomListUpdate.recipientId(), chatroomListUpdateDest, new ChatroomListDTO(chatroomListUpdate.chatrooms()));
      for (ChatroomShortInfoDTO chatroom : chatroomListUpdate.chatrooms()) {
        if (chatroomListUpdate.operation() == ChatroomListUpdateDTO.Operation.ADD) {
          userManagerService.addUserToChatroom(chatroomListUpdate.recipientId(), chatroom.id());
        } else {
          userManagerService.removeUserFromChatroom(chatroomListUpdate.recipientId(), chatroom.id());
        }
      }
    }
  }

  @Override
  public void addChatroomToUser(String userId, ChatroomListDTO list) {
    kafkaMessageTemplate.send(chatroomListTopic, new ChatroomListUpdateDTO(userId, list.chatrooms(), ChatroomListUpdateDTO.Operation.ADD));
  }

  @Override
  public void removeChatroomFromUser(String userId, String chatroomId) {
    kafkaMessageTemplate.send(chatroomListTopic, new ChatroomListUpdateDTO(userId, List.of(new ChatroomShortInfoDTO(chatroomId, null)), ChatroomListUpdateDTO.Operation.REMOVE));
  }

  @KafkaListener(id = "userListUpdater", topics = {"${chatapp.kafka.user-list-update-topic}"})
  void receiveUserListUpdate(UserListUpdateDTO userListUpdate) {
    if (userManagerService.isChatroomPresent(userListUpdate.chatroomId())) {
      for (String userId : userManagerService.getChatroomConnectedClients(userListUpdate.chatroomId())) {
        template.convertAndSendToUser(userId, chatroomUserListUpdDest, userListUpdate.chatroomId(), Map.of("operation", userListUpdate.operation().name().toLowerCase()));
      }
    }
  }

  @Override
  public void addUserToChatroom(String chatroomId, List<UserShortInfoDTO> users) {
    var chatroomUsers = chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList();
    kafkaMessageTemplate.send(userListTopic, new UserListUpdateDTO(chatroomUsers, chatroomId, users, ADD));
  }

  @Override
  public void addUserToChatroom(String chatroomId, String userId, List<UserShortInfoDTO> users) {
    kafkaMessageTemplate.send(userListTopic, new UserListUpdateDTO(List.of(userId), chatroomId, users, ADD));
  }

  @Override
  public void removeUserFromChatroom(String chatroomId, String userId) {
    var chatroomUsers = chatroomRepo.getUsersByChatroom(chatroomId).stream().map(ChatUser::id).toList();
    kafkaMessageTemplate.send(userListTopic, new UserListUpdateDTO(chatroomUsers, chatroomId, List.of(new UserShortInfoDTO(userId, null)), UserListUpdateDTO.Operation.REMOVE));
  }
}
