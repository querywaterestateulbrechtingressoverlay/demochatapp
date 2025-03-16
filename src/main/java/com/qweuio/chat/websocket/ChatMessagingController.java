package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
import com.qweuio.chat.persistence.MessagePersistingService;
import com.qweuio.chat.persistence.entity.ChatMessage;
import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.UserRole;
import com.qweuio.chat.persistence.repository.ChatMessageRepository;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.websocket.dto.*;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.exception.ChatroomAccessException;
import com.qweuio.chat.websocket.exception.TooManyChatroomsException;
import com.qweuio.chat.websocket.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

@Controller
public class ChatMessagingController {
  Logger logger = LoggerFactory.getLogger(ChatMessagingController.class);
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  ChatUserRepository userRepo;
  @Autowired
  MongoTemplate mongoTemplate;
  @Autowired
  ChatMessageRepository messageRepo;
  @Autowired
  MessagePersistingService messagePersistingService;
  @Autowired
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate messagingTemplate;

  void updateUserListForSubscribers(String chatroomId) {
    kafkaService.updateUserList(new ChatUserListDTO(chatroomId,
      chatroomRepo
        .findById(chatroomId).orElseThrow(() -> new RuntimeException("wtf"))
        .users().stream()
        .map((ur) -> userRepo.findById(ur.userId()).orElseThrow(() -> new RuntimeException("wtf")))
        .map(Converters::toDTO).toList()));
  }

  void updateChatroomListForSubscriber(String userId) {
    messagingTemplate.convertAndSendToUser(userId, "/chatrooms",
      new ChatroomListDTO(
        userRepo.findById(userId)
          .orElseThrow(() -> new RuntimeException("a ghost in the system has appeared")).chatroomIds().stream()
          .map((cid) -> chatroomRepo.findById(cid).get())
          .map(Converters::toDTO).toList()));
  }

  @MessageMapping("/getAvailableChatrooms")
  public void getChatrooms(@Headers Map<String, String> headers, Principal principal) {
    updateChatroomListForSubscriber(principal.getName());
  }

  @MessageMapping("/{chatId}/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message,
                          @DestinationVariable String chatId,
                          Principal principal) {
    Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());
    if (messageSender.isEmpty()) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), chatId, Instant.now(), message.message()));
    }
  }
  @MessageMapping("/{chatId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatId,
                               Principal principal) {
    Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());
    if (messageSender.isEmpty()) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/chatrooms/messages", new ChatHistoryResponseDTO(chatId, messagePersistingService.getRecentMessages(chatId)));
    }
  }
  @MessageMapping("/{chatId}/getUsers")
  public void getUsers(@DestinationVariable String chatId,
                       Principal principal) {
    Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());
    if (messageSender.isEmpty()) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      List<ChatUser> users = chatroomRepo.getUsersByChatroom(chatId);
      kafkaService.updateUserList(new ChatUserListDTO(chatId,
        users.stream().map(Converters::toDTO).toList()));
    }
  }
  @MessageMapping("/create")
  void createChat(@Payload ChatroomNameDTO chatCreationRequest, Principal principal) {
    if (userRepo.findById(principal.getName()).get().chatroomIds().size() < 100) {
      String newChatroomId = chatroomRepo.save(
        new Chatroom(
          null, chatCreationRequest.chatroomName(),
          List.of(new UserRole(principal.getName(), "ADMIN")),
          Collections.emptyList())).id();
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(principal)),
        new Update().addToSet("chatrooms", newChatroomId),
        "users");
      updateChatroomListForSubscriber(principal.getName());
    } else {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Can't create a chatroom due to user being a member of too many chatrooms");
    }
  }
  @MessageMapping("/{chatId}/delete")
  void deleteChat(@DestinationVariable String chatId, Principal principal) {
    try {
      Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());

      if (messageSender.isEmpty() || !Objects.equals(messageSender.get().role(), "ADMIN")) throw new ChatroomAccessException("Insufficient permissions");

      List<UserRole> chatUsers = chatroomRepo.findById(chatId).orElseThrow(() -> new ChatroomAccessException("Chatroom " + chatId + " couldn't be found")).users();
      chatroomRepo.deleteById(chatId);
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(principal)),
        new Update().pull("chatrooms", chatId),
        "users");
    } catch (ChatroomAccessException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
  @MessageMapping("/{chatId}/invite")
  void inviteUserToChat(@Payload UserIdDTO userToInvite,
                        @DestinationVariable String chatId,
                        Principal principal) {
    try {
      Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());
      if (messageSender.isEmpty() || !Objects.equals(messageSender.get().role(), "ADMIN")) throw new ChatroomAccessException("Insufficient permissions");

      ChatUser invitee = userRepo
        .findById(userToInvite.userId())
        .orElseThrow(() -> new UserNotFoundException(userToInvite.userId()));
      if (invitee.chatroomIds().size() < 100) {
        mongoTemplate.updateFirst(
          new Query(Criteria.where("_id").is(invitee.id())),
          new Update().addToSet("chatroomIds", chatId),
          "users");
        mongoTemplate.updateFirst(
          new Query(Criteria.where("_id").is(chatId)),
          new Update().addToSet("users", new UserRole(invitee.id(), "MEMBER")),
          "chatrooms");
        kafkaService.updateUserList(new ChatUserListDTO(chatId,
          chatroomRepo
            .findById(chatId).orElseThrow(() -> new RuntimeException("wtf"))
            .users().stream()
            .map((ur) -> userRepo.findById(ur.userId()).orElseThrow(() -> new RuntimeException("wtf")))
            .map(Converters::toDTO).toList()));
        updateUserListForSubscribers(chatId);
        updateChatroomListForSubscriber(userToInvite.userId());
      } else {
        throw new TooManyChatroomsException("Couldn't invite user because they've reached the chatroom count limit");
      }
    } catch (UserNotFoundException | TooManyChatroomsException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
  @MessageMapping("/{chatId}/kick")
  void kickUserFromChat(@Payload UserIdDTO userToKick,
                        @DestinationVariable String chatId,
                        Principal principal) {
    try {
      Optional<UserRole> messageSender = chatroomRepo.getUserRoleFromChatroomById(chatId, principal.getName());
      Optional<UserRole> kickTarget = chatroomRepo.getUserRoleFromChatroomById(chatId, userToKick.userId());

      if (messageSender.isEmpty() ||
        !Objects.equals(messageSender.get().role(), "ADMIN") ||
        kickTarget.isEmpty() ||
        Objects.equals(kickTarget.get().role(), "ADMIN)")) {
        throw new ChatroomAccessException("Insufficient permissions");
      }
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(kickTarget.get().userId())),
        new Update().pull("chatroomIds", chatId),
        "users");
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(chatId)),
        new Update().pull("users", Query.query(Criteria.where("userId").is(kickTarget.get().userId()))),
        "chatrooms");
      updateUserListForSubscribers(chatId);
      updateChatroomListForSubscriber(userToKick.userId());
    } catch (UserNotFoundException | ChatroomAccessException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
}
