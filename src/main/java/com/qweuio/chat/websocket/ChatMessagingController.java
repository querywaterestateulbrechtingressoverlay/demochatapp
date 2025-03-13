package com.qweuio.chat.websocket;

import com.qweuio.chat.messaging.KafkaService;
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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
  KafkaService kafkaService;
  @Autowired
  SimpMessagingTemplate messagingTemplate;

  boolean checkUserMembership(String userId, String chatroomId) {
    Optional<Chatroom> destChatroom = chatroomRepo.findById(chatroomId);
    if (destChatroom.isPresent()) {
      Optional<UserRole> user = destChatroom.get().users().stream().filter((ur) -> Objects.equals(ur.userId(), userId)).findFirst();
      if (user.isPresent()) {
        return true;
      }
    }
    return false;
  }

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
  public void getChatrooms(Principal principal) {
    updateChatroomListForSubscriber(principal.getName());
  }

  @MessageMapping("/{chatId}/send")
  public void sendMessage(@Payload UnprocessedMessageDTO message,
                          @DestinationVariable String chatId,
                          Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.sendMessage(new ProcessedMessageDTO(principal.getName(), chatId, message.message()));
    }
  }
  @MessageMapping("/{chatId}/getRecentHistory")
  public void getRecentHistory(@Payload MessageHistoryRequestDTO messageRequest,
                               @DestinationVariable String chatId,
                               Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      List<ChatMessage> foundMessages = messageRequest.beforeMessageId() == null ?
        messageRepo.findTop10ByChatroomId(chatId) :
        messageRepo.findTop10ByChatroomIdAndIdLessThan(chatId, messageRequest.beforeMessageId());
    }
  }
  @MessageMapping("/{chatId}/getUsers")
  public void getUsers(@DestinationVariable String chatId,
                       Principal principal) {
    if (!checkUserMembership(principal.getName(), chatId)) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Provided chatroom id either doesn't exist or you don't have the rights to post there");
    } else {
      kafkaService.updateUserList(new ChatUserListDTO(chatId,
        chatroomRepo
        .findById(chatId).orElseThrow(() -> new RuntimeException("wtf"))
        .users().stream()
        .map((ur) -> userRepo.findById(ur.userId()).orElseThrow(() -> new RuntimeException("wtf")))
        .map(Converters::toDTO).toList()));
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
      List<UserRole> chatUsers = chatroomRepo.findById(chatId).orElseThrow(() -> new ChatroomAccessException("Chatroom " + chatId + " couldn't be found")).users();
      chatUsers.stream()
        .filter((ur) -> Objects.equals(ur.userId(), principal.getName()) && Objects.equals(ur.role(), "ADMIN"))
        .findAny().orElseThrow(() -> new ChatroomAccessException("You don't have the permission to delete this chatroom"));
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
      if (!checkUserMembership(principal.getName(), chatId)) {
        throw new ChatroomAccessException("Provided chatroom id either doesn't exist or you are not a member of it");
      }
      Optional<UserRole> inviter = chatroomRepo.findById(chatId).get().users().stream().filter((u) -> Objects.equals(u.userId(), principal.getName())).findFirst();
      if (!Objects.equals(inviter.get().role(), "ADMIN")) {
        throw new ChatroomAccessException("You are not an admin of the chatroom you are inviting the user into");
      }
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
      if (!checkUserMembership(principal.getName(), chatId)) {
        throw new ChatroomAccessException("Provided chatroom id either doesn't exist or you are not a member of it");
      }
      Optional<UserRole> kickingUser = chatroomRepo.findById(chatId).get().users().stream().filter((u) -> Objects.equals(u.userId(), principal.getName())).findFirst();
      if (!Objects.equals(kickingUser.get().role(), "ADMIN")) {
        throw new ChatroomAccessException("You are not an admin of the chatroom you are kicking the user from");
      }
      Optional<UserRole> kickedUser = chatroomRepo.findById(chatId).get().users().stream().filter((u) -> Objects.equals(u.userId(), userToKick.userId())).findFirst();
      if (kickedUser.isEmpty()) {
        throw new UserNotFoundException(userToKick.userId());
      }
      if (kickedUser.get().role().equals("ADMIN")) {
        throw new RuntimeException("Can't kick an admin user from the chatroom");
      }
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(kickedUser.get().userId())),
        new Update().pull("chatroomIds", chatId),
        "users");
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(chatId)),
        new Update().pull("users", Query.query(Criteria.where("userId").is(kickedUser.get().userId()))),
        "chatrooms");
      updateUserListForSubscribers(chatId);
      updateChatroomListForSubscriber(userToKick.userId());
    } catch (UserNotFoundException | ChatroomAccessException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
}
