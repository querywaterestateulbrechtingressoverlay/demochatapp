package com.qweuio.chat.websocket;

import com.qweuio.chat.core.WebSocketChatMessagingService;
import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.UserWithRoleEntity;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

@Controller
@MessageMapping
public class ChatMessagingController {
  Logger logger = LoggerFactory.getLogger(ChatMessagingController.class);
  @Autowired
  MessagePersistingService messagePersistingService;
  @Autowired
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate messagingTemplate;
  @Autowired
  WebSocketChatMessagingService chatService;

  @MessageExceptionHandler({UserActionException.class})
  void userActionException(UserActionException exception) {
    logger.debug(exception.getMessage());
  }
  void updateUserListForSubscribers(String chatroomId) {
    kafkaService.updateUserList(new ChatUserListDTO(chatroomId,
      chatService.getChatroomUsers(chatroomId).stream()
        .map(Converters::toDTO)
        .toList()));
  }

  void updateChatroomListForSubscriber(String userId) {
    messagingTemplate.convertAndSendToUser(userId, "/chatrooms",
      new ChatroomListDTO(
        chatService.getUserChatrooms(userId).stream()
          .map((c) -> new ChatroomShortInfoDTO(c.id(), c.name()))
          .toList()));
  }

  @MessageMapping("/getAvailableChatrooms")
  public void getChatrooms(@Headers Map<String, String> headers, Principal principal) {
    updateChatroomListForSubscriber(principal.getName());
  }

  @MessageMapping("/{chatroomId}/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message,
                          @DestinationVariable String chatroomId,
                          Principal principal) {
    if (chatService.verifyUserRole(principal.getName(), chatroomId, WebSocketChatMessagingService.UserRole.NOT_A_MEMBER)) {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), chatroomId, Instant.now(), message.message()));
    }
  }
  @MessageMapping("/{chatroomId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatroomId,
                               Principal principal) {
    List<ProcessedMessageDTO> messages = chatService.getChatroomRecentHistory(principal.getName(), chatroomId);
    messagingTemplate.convertAndSendToUser(principal.getName(), "/chatrooms/messages", new ChatHistoryResponseDTO(chatroomId, messagePersistingService.getRecentMessages(chatroomId)));
  }
  @MessageMapping("/{chatroomId}/getUsers")
  public void getUsers(@DestinationVariable String chatroomId,
                       Principal principal) {
    var asdf = chatService.getChatroomUsers(principal.getName(), chatroomId).stream()
      .map((c) -> new UserShortInfoDTO(c.id(), c.name()))
      .toList();
    kafkaService.updateUserList(new ChatUserListDTO(chatroomId, asdf));
  }
  @MessageMapping("/create")
  void createChat(@Payload ChatroomNameDTO chatCreationRequest, Principal principal) {
    chatService.createChatroom(principal.getName(), chatCreationRequest.chatroomName());
  }
  @MessageMapping("/{chatroomId}/delete")
  void deleteChat(@DestinationVariable String chatroomId, Principal principal) {
    chatService.deleteChatroom(principal.getName(), chatroomId);
  }
  @MessageMapping("/{chatroomId}/invite")
  void inviteUserToChat(@Payload UserIdDTO userToInvite,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    chatService.addUserToChatroom(principal.getName(), userToInvite.userId(), chatroomId);
    updateUserListForSubscribers(chatroomId);
    updateChatroomListForSubscriber(userToInvite.userId());
  }
  @MessageMapping("/{chatroomId}/kick")
  void kickUserFromChat(@Payload UserIdDTO userToKick,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    chatService.removeUserFromChatroom(principal.getName(), userToKick.userId(), chatroomId);
    updateUserListForSubscribers(chatroomId);
    updateChatroomListForSubscriber(userToKick.userId());
  }
}
