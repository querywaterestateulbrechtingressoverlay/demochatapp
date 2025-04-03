package com.qweuio.chat.websocket;

import com.qweuio.chat.core.MongoChatMessagingService;
import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.websocket.dto.inbound.MessageHistoryRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

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
  MongoChatMessagingService chatService;
  @Autowired
  MessageSenderService senderService;

  @MessageExceptionHandler({UserActionException.class})
  void userActionException(UserActionException exception) {
    logger.debug(exception.getMessage());
  }

  @MessageMapping("/getAvailableChatrooms")
  public void getChatrooms(@Headers Map<String, String> headers, Principal principal) {
    senderService.addChatroomToUser(principal.getName(), Converters.toDTO(chatService.getUserChatrooms(principal.getName())));
  }

  @MessageMapping("/{chatroomId}/send")
  public void sendMessage(@Payload MessageRequestDTO message,
                          @DestinationVariable String chatroomId,
                          Principal principal) {
    messagePersistingService.persistMessage();
    senderService.sendMessage();
    if (chatService.verifyUserRole(, MongoChatMessagingService.UserRole.NOT_A_MEMBER)) {
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
    List<ChatUser> users = chatService.getChatroomUsers(principal.getName(), chatroomId);

  }

  @MessageMapping("/create")
  void createChat(@Payload ChatroomNameDTO chatCreationRequest, Principal principal) {
    chatService.createChatroom(principal.getName(), chatCreationRequest.chatroomName());
    updateChatroomListForUser(principal.getName());
  }

  @MessageMapping("/{chatroomId}/delete")
  void deleteChat(@DestinationVariable String chatroomId, Principal principal) {
    List<ChatUser> users = chatService.getChatroomUsers(chatroomId);
    chatService.deleteChatroom(principal.getName(), chatroomId);
    users.forEach((cu) -> updateChatroomListForUser(cu.id()));
  }

  @MessageMapping("/{chatroomId}/invite")
  void inviteUserToChat(@Payload UserIdDTO userToInvite,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    chatService.addUserToChatroom(principal.getName(), userToInvite.userId(), chatroomId);
    senderService.addUserToChatroom(chatroomId, chatService.getChatroomUsers(chatroomId), chatService.getUser);
    senderService.addChatroomToUser(userToInvite.userId(), List.of(new ChatroomShortInfoDTO()));
  }

  @MessageMapping("/{chatroomId}/kick")
  void kickUserFromChat(@Payload UserIdDTO userToKick,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    chatService.removeUserFromChatroom(principal.getName(), userToKick.userId(), chatroomId);
    senderService.removeUserFromChatroom(chatroomId, userToKick.userId());
    senderService.removeChatroomFromUser(userToKick.userId(), chatroomId);
  }
}
