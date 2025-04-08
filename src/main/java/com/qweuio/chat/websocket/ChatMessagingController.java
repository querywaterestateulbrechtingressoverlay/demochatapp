package com.qweuio.chat.websocket;

import com.qweuio.chat.core.MongoChatMessagingService;
import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
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
    logger.info("get av chatrooms, user {}", principal.getName());
    senderService.addChatroomToUser(principal.getName(), Converters.toDTO(chatService.getUserChatrooms(principal.getName())));
  }

  @MessageMapping("/{chatroomId}/send")
  public void sendMessage(@Payload MessageRequestDTO message,
                          @DestinationVariable String chatroomId,
                          Principal principal) {
    logger.info("send message, user {}", principal.getName());
    ChatMessage processedMessage = chatService.sendMessage(principal.getName(), chatroomId, message);
    senderService.sendMessage(
      processedMessage,
      chatService.getChatroomUsers(chatroomId)
        .stream()
        .map(ChatUser::id)
        .toList());
  }

  @MessageMapping("/{chatroomId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatroomId,
                               Principal principal) {
    List<ChatMessage> messages = chatService.getChatroomRecentHistory(principal.getName(), chatroomId);
    senderService.updateMessageHistory(principal.getName(), chatroomId, messages);
  }

  @MessageMapping("/{chatroomId}/getUsers")
  public void getUsers(@DestinationVariable String chatroomId,
                       Principal principal) {
    List<ChatUser> users = chatService.getChatroomUsers(principal.getName(), chatroomId);
    senderService.addUsersToChatroom(chatroomId, principal.getName(), users.stream().map(Converters::toDTO).toList());
  }

  @MessageMapping("/create")
  void createChat(@Payload ChatroomNameDTO chatCreationRequest, Principal principal) {
    Chatroom newChatroom = chatService.createChatroom(principal.getName(), chatCreationRequest.chatroomName());
    senderService.addChatroomToUser(principal.getName(), Converters.toDTO(List.of(newChatroom)));
  }

  @MessageMapping("/{chatroomId}/delete")
  void deleteChat(@DestinationVariable String chatroomId, Principal principal) {
    List<ChatUser> users = chatService.getChatroomUsers(chatroomId);
    chatService.deleteChatroom(principal.getName(), chatroomId);
    users.forEach((cu) -> senderService.removeChatroomFromUser(cu.id(), chatroomId));
  }

  @MessageMapping("/{chatroomId}/invite")
  void inviteUserToChat(@Payload UserIdDTO userToInvite,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    Chatroom chatroom = chatService.addUserToChatroom(principal.getName(), userToInvite.userId(), chatroomId);
    senderService.addUserToChatroom(
      chatroomId,
      chatService.getChatroomUsers(chatroomId)
        .stream()
        .map(ChatUser::id)
        .toList(),
      Converters.toDTO(chatService
        .getUserInfo(userToInvite.userId())));
    senderService.addChatroomToUser(userToInvite.userId(), Converters.toDTO(List.of(chatroom)));
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
