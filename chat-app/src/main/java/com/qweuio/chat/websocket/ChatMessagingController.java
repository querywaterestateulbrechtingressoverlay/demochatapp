package com.qweuio.chat.websocket;

import com.qweuio.chat.core.UserChatService;
import com.qweuio.chat.core.exception.*;
import com.qweuio.chat.core.exception.chatapp.UserIdNotFoundException;
import com.qweuio.chat.core.exception.chatapp.UserNameNotFoundException;
import com.qweuio.chat.persistence.entity.Message;
import com.qweuio.chat.persistence.entity.User;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.websocket.dto.inbound.*;
import com.qweuio.chat.websocket.dto.outbound.ErrorResponseDTO;
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
  UserChatService chatService;
  @Autowired
  KafkaMessageSenderService senderService;

  @MessageExceptionHandler({UserActionException.class})
  void userActionException(Principal principal, UserActionException exception) {
    senderService.sendErrorMessage(new ErrorResponseDTO(UUID.fromString(principal.getName()), Instant.now(), exception.getMessage()));
  }

  @MessageMapping("/getAvailableChatrooms")
  public void getChatrooms(Principal principal) {
    logger.info("get av chatrooms, user {}", UUID.fromString(principal.getName()));
    senderService.addChatroomToUser(UUID.fromString(principal.getName()), Converters.toDTO(chatService.getUserChatrooms(UUID.fromString(principal.getName()))));
  }

  @MessageMapping("/{chatroomId}/send")
  public void sendMessage(@Payload MessageRequestDTO message,
                          @DestinationVariable String chatroomId,
                          Principal principal) {
    logger.info("send message, user {}, chatroom {}", UUID.fromString(principal.getName()), UUID.fromString(chatroomId));
    Message processedMessage = chatService.saveMessage(UUID.fromString(principal.getName()), UUID.fromString(chatroomId), message);
    senderService.sendMessage(processedMessage, UUID.fromString(chatroomId));
  }

  @MessageMapping("/{chatroomId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatroomId,
                               Principal principal) {
    List<Message> messages = chatService.getChatroomRecentHistory(UUID.fromString(principal.getName()), UUID.fromString(chatroomId));
    senderService.updateMessageHistory(UUID.fromString(principal.getName()), UUID.fromString(chatroomId), messages);
  }

  @MessageMapping("/{chatroomId}/getUsers")
  public void getUsers(@DestinationVariable String chatroomId,
                       Principal principal) {
    List<User> users = chatService.getChatroomUsers(UUID.fromString(principal.getName()), UUID.fromString(chatroomId));
    logger.info(users.toString());
    senderService.addUserToChatroom(UUID.fromString(chatroomId), users.stream().map(Converters::toDTO).toList());
  }

  @MessageMapping("/create")
  void createChat(@Payload ChatroomNameDTO chatCreationRequest, Principal principal) {
    Chatroom newChatroom = chatService.createChatroom(UUID.fromString(principal.getName()), chatCreationRequest.chatroomName());
    senderService.addChatroomToUser(UUID.fromString(principal.getName()), Converters.toDTO(List.of(newChatroom)));
  }

  @MessageMapping("/{chatroomId}/delete")
  void deleteChat(@DestinationVariable String chatroomId, Principal principal) {
    List<User> users = chatService.getChatroomUsers(UUID.fromString(chatroomId));
    chatService.deleteChatroom(UUID.fromString(principal.getName()), UUID.fromString(chatroomId));
    users.forEach((cu) -> senderService.removeChatroomFromUser(cu.id(), UUID.fromString(chatroomId)));
  }

  @MessageMapping("/{chatroomId}/invite")
  void inviteUserToChat(@Payload UserNameDTO userToInvite,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    User user = chatService.findUserByUsername(userToInvite.username()).orElseThrow(() -> new UserNameNotFoundException(userToInvite.username()));
    Chatroom chatroom = chatService.addUserToChatroom(UUID.fromString(principal.getName()), user.id(), UUID.fromString(chatroomId));
    senderService.addUserToChatroom(UUID.fromString(chatroomId), List.of(Converters.toDTO(chatService.getUserInfo(user.id()))));
    senderService.addChatroomToUser(user.id(), Converters.toDTO(List.of(chatroom)));
  }

  @MessageMapping("/{chatroomId}/kick")
  void kickUserFromChat(@Payload UserIdDTO userToKick,
                        @DestinationVariable String chatroomId,
                        Principal principal) {
    chatService.removeUserFromChatroom(UUID.fromString(principal.getName()), userToKick.userId(), UUID.fromString(chatroomId));
    senderService.removeUserFromChatroom(UUID.fromString(chatroomId), userToKick.userId());
    senderService.removeChatroomFromUser(userToKick.userId(), UUID.fromString(chatroomId));
  }
}
