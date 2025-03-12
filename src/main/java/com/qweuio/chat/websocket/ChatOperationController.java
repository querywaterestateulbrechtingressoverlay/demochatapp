package com.qweuio.chat.websocket;

import com.qweuio.chat.persistence.entity.ChatUser;
import com.qweuio.chat.persistence.entity.Chatroom;
import com.qweuio.chat.persistence.entity.UserRole;
import com.qweuio.chat.persistence.repository.ChatUserRepository;
import com.qweuio.chat.persistence.repository.ChatroomRepository;
import com.qweuio.chat.websocket.dto.ChatIdDTO;
import com.qweuio.chat.websocket.dto.ChatroomNameDTO;
import com.qweuio.chat.websocket.dto.UserAndChatDTO;
import com.qweuio.chat.websocket.dto.UserIdDTO;
import com.qweuio.chat.websocket.exception.ChatroomAccessException;
import com.qweuio.chat.websocket.exception.TooManyChatroomsException;
import com.qweuio.chat.websocket.exception.UserNotFoundException;
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
import java.util.Optional;

@Controller
public class ChatOperationController {
  @Autowired
  ChatroomRepository chatroomRepo;
  @Autowired
  ChatUserRepository userRepo;
  @Autowired
  MongoTemplate mongoTemplate;
  @Autowired
  SimpMessagingTemplate messagingTemplate;
  boolean checkUserMembership(String userId, String chatroomId) {
    Optional<Chatroom> destChatroom = chatroomRepo.findById(chatroomId);
    return destChatroom.isPresent() && destChatroom.get().users().stream().anyMatch((ur) -> ur.userId() == userId);
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
        new Update().addToSet("chatroomIds", newChatroomId),
        "users");
    } else {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", "Can't create a chatroom due to user being a member of too many chatrooms");
    }
  }
  @MessageMapping("/{chatId}/delete")
  void deleteChat(@DestinationVariable String chatId, Principal principal) {
    try {;
      List<UserRole> chatUsers = chatroomRepo.findById(chatId).orElseThrow(() -> new ChatroomAccessException("Chatroom " + chatId + " couldn't be found")).users();
      chatUsers.stream()
        .filter((ur) -> ur.userId() == principal.getName() && ur.role() == "ADMIN")
        .findAny().orElseThrow(() -> new ChatroomAccessException("You don't have the permission to delete this chatroom"));
      chatroomRepo.deleteById(chatId);
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(principal)),
        new Update().pull("chatroomIds", chatId),
        "users");
    } catch (ChatroomAccessException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
  @MessageMapping("/{chatId}/invite")
  void inviteUserToChat(@Payload UserIdDTO userInvitationRequest,
                        @DestinationVariable String chatId,
                        Principal principal) {
    try {
      if (!checkUserMembership(principal.getName(), chatId)) {
        throw new ChatroomAccessException("Provided chatroom id either doesn't exist or you are not a member of it");
      }
      Optional<UserRole> inviter = chatroomRepo.findById(chatId).get().users().stream().filter((u) -> u.userId() == principal.getName()).findFirst();
      if (inviter.get().role() != "ADMIN") {
        throw new ChatroomAccessException("You are not an admin of the chatroom you are inviting the user into");
      }
      ChatUser invitee = userRepo
      .findById(userInvitationRequest.userId())
      .orElseThrow(() -> new UserNotFoundException(userInvitationRequest.userId()));
      if (invitee.chatroomIds().size() < 100) {
        mongoTemplate.updateFirst(
          new Query(Criteria.where("_id").is(invitee.id())),
          new Update().addToSet("chatroomIds", chatId),
          "users");
        mongoTemplate.updateFirst(
          new Query(Criteria.where("_id").is(chatId)),
          new Update().addToSet("users", new UserRole(invitee.id(), "MEMBER")),
          "users");
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
      Optional<UserRole> kickingUser = chatroomRepo.findById(chatId).get().users().stream().filter((u) -> u.userId() == principal.getName()).findFirst();
      if (kickingUser.get().role() != "ADMIN") {
        throw new ChatroomAccessException("You are not an admin of the chatroom you are kicking the user from");
      }
      ChatUser kickedUser = userRepo
        .findById(userToKick.userId())
        .orElseThrow(() -> new UserNotFoundException(userToKick.userId()));
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(kickedUser.id())),
        new Update().pull("chatroomIds", chatId),
        "users");
      mongoTemplate.updateFirst(
        new Query(Criteria.where("_id").is(chatId)),
        new Update().pull("users", new UserRole(kickedUser.id(), "MEMBER")),
        "users");
    } catch (UserNotFoundException | TooManyChatroomsException e) {
      messagingTemplate.convertAndSendToUser(principal.getName(), "/system", e.getMessage());
    }
  }
}
